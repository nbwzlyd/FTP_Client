package ftpcom.lyd.ftpandroidclient;

import android.os.Handler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;


import com.socket.ftpdome.constant.FTPConstant;
import com.socket.ftpdome.entity.FTPEntity;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class FTP {


    private FTPEntity ftpEntity;
    private Handler mHandler;
    // -------------------------------------------------------文件上传方法------------------------------------------------

    public FTP(FTPEntity ftpEntity) {
        this.ftpEntity = ftpEntity;
        mHandler = new Handler();
    }

    /**
     * 上传单个文件.
     *
     * @param localFile  本地文件
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadSingleFile(File singleFile, String remotePath,
                                 UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;
        flag = uploadingSingle(singleFile, listener);
        if (flag) {
            listener.onUploadProgress(FTPConstant.FTP_UPLOAD_SUCCESS, 0,
                    singleFile);
        } else {
            listener.onUploadProgress(FTPConstant.FTP_UPLOAD_FAIL, 0,
                    singleFile);
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传多个文件.
     *
     * @param localFile  本地文件
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadMultiFile(LinkedList<File> fileList, String remotePath,
                                UploadProgressListener listener) throws IOException {

        // 上传之前初始化
        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        for (File singleFile : fileList) {
            flag = uploadingSingle(singleFile, listener);
            if (flag) {
                listener.onUploadProgress(FTPConstant.FTP_UPLOAD_SUCCESS, 0,
                        singleFile);
            } else {
                listener.onUploadProgress(FTPConstant.FTP_UPLOAD_FAIL, 0,
                        singleFile);
            }
        }

        // 上传完成之后关闭连接
        this.uploadAfterOperate(listener);
    }

    /**
     * 上传单个文件.
     *
     * @param localFile 本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile,
                                    UploadProgressListener listener) throws IOException {
        boolean flag = true;
        // 不带进度的方式
        // // 创建输入流
        // InputStream inputStream = new FileInputStream(localFile);
        // // 上传单个文件
        // flag = ftpClient.storeFile(localFile.getName(), inputStream);
        // // 关闭文件流
        // inputStream.close();

        // 带有进度的方式
        BufferedInputStream buffIn = new BufferedInputStream(
                new FileInputStream(localFile));
        ProgressInputStream progressInput = new ProgressInputStream(buffIn,
                listener, localFile);
        flag = FtpManger.getInstance().storeFile(localFile.getName(), progressInput);
        buffIn.close();

        return flag;
    }

    /**
     * 上传文件之前初始化相关参数
     *
     * @param remotePath FTP目录
     * @param listener   监听器
     * @throws IOException
     */
    private void uploadBeforeOperate(final String remotePath,
                                     final UploadProgressListener listener) throws IOException {

        // 打开FTP服务
        this.openConnect(new LoginListener() {
            @Override
            public void loginSuccess() {
                listener.onUploadProgress(FTPConstant.FTP_CONNECT_SUCCESSS, 0,
                        null);
                // 设置模式
                try {
                    FtpManger.getInstance().setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
                    // FTP下创建文件夹
                    FtpManger.getInstance().makeDirectory(remotePath);
                    // 改变FTP目录
                    FtpManger.getInstance().changeWorkingDirectory(remotePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 上传单个文件
            }

            @Override
            public void loginFail() {
                listener.onUploadProgress(FTPConstant.FTP_CONNECT_FAIL, 0, null);

            }
        });


    }

    /**
     * 上传完成之后关闭连接
     *
     * @param listener
     * @throws IOException
     */
    private void uploadAfterOperate(UploadProgressListener listener)
            throws IOException {
        this.closeConnect();
        listener.onUploadProgress(FTPConstant.FTP_DISCONNECT_SUCCESS, 0, null);
    }

    // -------------------------------------------------------文件下载方法------------------------------------------------

    /**
     * 下载单个文件，可实现断点下载.
     *
     * @param serverPath Ftp目录及文件路径
     * @param localPath  本地目录
     * @param fileName   下载之后的文件名称
     * @param listener   监听器
     * @throws IOException
     */
    public void downloadSingleFile(final String serverPath, final String localPath, final String fileName, final DownLoadProgressListener listener) {

        // 打开FTP服务
            this.openConnect(new LoginListener() {
                @Override
                public void loginSuccess() {
                    listener.onDownLoadProgress(FTPConstant.FTP_CONNECT_SUCCESSS, 0, null);

                    // 先判断服务器文件是否存在
                    FTPFile[] files = new FTPFile[0];
                    try {
                        files = FtpManger.getInstance().listFiles(serverPath);
                        if (files.length == 0) {
                            listener.onDownLoadProgress(FTPConstant.FTP_FILE_NOTEXISTS, 0, null);
                            return;
                        }

                        //创建本地文件夹
                        File mkFile = new File(localPath);
                        if (!mkFile.exists()) {
                            mkFile.mkdirs();
                        }

                        String new_localPath = localPath + fileName;
                        // 接着判断下载的文件是否能断点下载
                        long serverSize = files[0].getSize(); // 获取远程文件的长度
                        File localFile = new File(new_localPath);
                        long localSize = 0;
                        if (localFile.exists()) {
                            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
                            if (localSize >= serverSize) {
                                File file = new File(new_localPath);
                                file.delete();
                            }
                        }

                        // 进度
                        long step = serverSize / 100;
                        long process = 0;
                        long currentSize = 0;
                        // 开始准备下载文件
                        OutputStream out = new FileOutputStream(localFile, true);
                        FtpManger.getInstance().setRestartOffset(localSize);
                        InputStream input = FtpManger.getInstance().retrieveFileStream(serverPath);
                        byte[] b = new byte[1024];
                        int length = 0;
                        while ((length = input.read(b)) != -1) {
                            out.write(b, 0, length);
                            currentSize = currentSize + length;
                            if (currentSize / step != process) {
                                process = currentSize / step;
                                if (process % 5 == 0) {  //每隔%5的进度返回一次
                                    listener.onDownLoadProgress(FTPConstant.FTP_DOWN_LOADING, process, null);
                                }
                            }
                        }
                        out.flush();
                        out.close();
                        input.close();

                        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
                        if (FtpManger.getInstance().completePendingCommand()) {
                            listener.onDownLoadProgress(FTPConstant.FTP_DOWN_SUCCESS, 0, new File(new_localPath));
                        } else {
                            listener.onDownLoadProgress(FTPConstant.FTP_DOWN_FAIL, 0, null);
                        }

                        // 下载完成之后关闭连接
                        closeConnect();
                        listener.onDownLoadProgress(FTPConstant.FTP_DISCONNECT_SUCCESS, 0, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void loginFail() {
                    listener.onDownLoadProgress(FTPConstant.FTP_CONNECT_FAIL, 0, null);

                }
            });
        }

    // -------------------------------------------------------文件删除方法------------------------------------------------

    /**
     * 删除Ftp下的文件.
     *
     * @param serverPath Ftp目录及文件路径
     * @param listener   监听器
     * @throws IOException
     */
    public void deleteSingleFile(final String serverPath, final DeleteFileProgressListener listener) {

        // 打开FTP服务
        this.openConnect(new LoginListener() {
            @Override
            public void loginSuccess() {
                listener.onDeleteProgress(FTPConstant.FTP_CONNECT_SUCCESSS);
                // 先判断服务器文件是否存在
                FTPFile[] files = new FTPFile[0];
                try {
                    files = FtpManger.getInstance().listFiles(serverPath);
                    if (files.length == 0) {
                        listener.onDeleteProgress(FTPConstant.FTP_FILE_NOTEXISTS);
                        return;
                    }

                    //进行删除操作
                    boolean flag = true;
                    flag = FtpManger.getInstance().deleteFile(serverPath);
                    if (flag) {
                        listener.onDeleteProgress(FTPConstant.FTP_DELETEFILE_SUCCESS);
                    } else {
                        listener.onDeleteProgress(FTPConstant.FTP_DELETEFILE_FAIL);
                    }

                    // 删除完成之后关闭连接
                    closeConnect();
                    listener.onDeleteProgress(FTPConstant.FTP_DISCONNECT_SUCCESS);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void loginFail() {
                listener.onDeleteProgress(FTPConstant.FTP_CONNECT_FAIL);

            }
        });
    }

    // -------------------------------------------------------打开关闭连接------------------------------------------------

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect(final LoginListener loginListener) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FtpManger.getInstance().setControlEncoding("UTF-8");
                int reply; // 服务器响应值
                try {
                    // 连接至服务器
                    FtpManger.getInstance().connect(ftpEntity.hostName, ftpEntity.serverPort);
                    reply = FtpManger.getInstance().getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        // 断开连接
                        FtpManger.getInstance().disconnect();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (loginListener != null) {
                                    loginListener.loginFail();
                                }
                            }
                        });
                    }

                    // 登录到服务器
                    FtpManger.getInstance().login(ftpEntity.userName, ftpEntity.password);
                    // 获取响应值
                    reply = FtpManger.getInstance().getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        // 断开连接
                        FtpManger.getInstance().disconnect();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (loginListener != null) {
                                    loginListener.loginFail();
                                }
                            }
                        });

                    } else {
                        // 获取登录信息
                        FTPClientConfig config = new FTPClientConfig(FtpManger.getInstance()
                                .getSystemType().split(" ")[0]);
                        config.setServerLanguageCode("zh");
                        FtpManger.getInstance().configure(config);
                        // 使用被动模式设为默认
                        FtpManger.getInstance().enterLocalPassiveMode();
                        // 二进制文件支持
                        FtpManger.getInstance().setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (loginListener != null) {
                                    loginListener.loginSuccess();
                                }
                            }
                        });
                    }
                }  catch (IOException e1) {
                    e1.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (loginListener != null) {
                                loginListener.loginFail();
                            }
                        }
                    });
                }
            }

        });
        thread.start();
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (FtpManger.getInstance() != null) {
            // 退出FTP
            FtpManger.getInstance().logout();
            // 断开连接
            FtpManger.getInstance().disconnect();
        }
    }

    // ---------------------------------------------------上传、下载、删除监听---------------------------------------------

    /*
     * 上传进度监听
     */
    public interface UploadProgressListener {
        public void onUploadProgress(String currentStep, long uploadSize, File file);
    }

    /*
     * 下载进度监听
     */
    public interface DownLoadProgressListener {
        public void onDownLoadProgress(String currentStep, long downProcess, File file);
    }

    /*
     * 文件删除监听
     */
    public interface DeleteFileProgressListener {
        public void onDeleteProgress(String currentStep);
    }

    public interface LoginListener {
        void loginSuccess();

        void loginFail();
    }

    public static class FtpManger {
        private static FTPClient ftpClient;

        public static FTPClient getInstance() {
            if (ftpClient == null) {
                ftpClient = new FTPClient();
            }
            return ftpClient;
        }

        private FtpManger() {
        }
    }

}
