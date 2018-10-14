package ftpcom.lyd.ftpandroidclient.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.socket.ftpdome.entity.FTPEntity;
import com.socket.ftpdome.view.SwitchView;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import ftpcom.lyd.ftpandroidclient.FTP;
import ftpcom.lyd.ftpandroidclient.R;
import ftpcom.lyd.ftpandroidclient.chain.AccountChain;
import ftpcom.lyd.ftpandroidclient.chain.IpChain;
import ftpcom.lyd.ftpandroidclient.chain.LoginChain;
import ftpcom.lyd.ftpandroidclient.chain.PasswordChain;
import ftpcom.lyd.ftpandroidclient.chain.PortChain;
import ftpcom.lyd.ftpandroidclient.util.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView mAccountView, mPasswordView, mLoginView;
    private EditText mET_IPAddress, mET_IPport, mET_Account, mET_password;
    private SwitchView mSwitchView;
    private boolean isAnonymous;
    private FTP mFTPClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        int drawable = Color.parseColor("#1590D7");
        ColorDrawable actionbarD = new ColorDrawable(drawable);
        getSupportActionBar().setTitle("ftp传输工具");
        getSupportActionBar().setBackgroundDrawable(actionbarD);


        mAccountView = (TextView) findViewById(R.id.account);
        mPasswordView = (TextView) findViewById(R.id.password);
        mSwitchView = (SwitchView) findViewById(R.id.switch_view);
        mSwitchView.setEnable(true);

        mET_IPAddress = (EditText) findViewById(R.id.et_address);
        mET_IPport = (EditText) findViewById(R.id.et_server_port);
        mET_Account = (EditText) findViewById(R.id.et_account);
        mET_password = (EditText) findViewById(R.id.et_password);
        mLoginView = (TextView) findViewById(R.id.login);
        mSwitchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                isAnonymous = true;
                mAccountView.setTextColor(Color.parseColor("#F2F6F6"));
                mPasswordView.setTextColor(Color.parseColor("#F2F6F6"));
                mET_Account.setVisibility(View.INVISIBLE);
                mET_password.setVisibility(View.INVISIBLE);

            }

            @Override
            public void toggleToOff(View view) {
                isAnonymous = false;
                mAccountView.setTextColor(Color.parseColor("#1590D7"));
                mPasswordView.setTextColor(Color.parseColor("#1590D7"));
                mET_Account.setVisibility(View.VISIBLE);
                mET_password.setVisibility(View.VISIBLE);
            }
        });


        mLoginView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               boolean isOk =  doWork(isAnonymous);
               if (isOk){
                login();
               }

            }
        });
    }

    private boolean doWork(boolean isAnonymous) {
        if (!isAnonymous) {
            final LoginChain ipChain = new IpChain(mET_IPAddress);
            final LoginChain portChain = new PortChain(mET_IPport);
            final LoginChain accountChain = new AccountChain(mET_Account);
            final LoginChain passwordChain = new PasswordChain(mET_password);
            ipChain.setNext(portChain).setNext(accountChain).setNext(passwordChain);
            return ipChain.doWork();
        } else {
            final LoginChain ipChain = new IpChain(mET_IPAddress);
            final LoginChain portChain = new PortChain(mET_IPport);
            ipChain.setNext(portChain);
            return ipChain.doWork();

        }
    }

    private void login() {
        FTPEntity ftpEntity = new FTPEntity();
        ftpEntity.hostName = mET_IPAddress.getText().toString().replace(" ","");
        ftpEntity.serverPort =Integer.valueOf(mET_IPport.getText().toString().replace(" ",""));
        ftpEntity.userName = mET_Account.getText().toString();
        ftpEntity.password = mET_password.getText().toString();
//        ftpEntity.hostName = "192.168.2.1";
//        ftpEntity.serverPort = 21;
//        ftpEntity.userName = "admin";
//        ftpEntity.password = "1378186";
        mFTPClient = new FTP(ftpEntity);

        mFTPClient.openConnect(new FTP.LoginListener() {
            @Override
            public void loginSuccess() {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, FileListActivity.class);
                startActivity(intent);
            }

            @Override
            public void loginFail() {
                ToastUtil.showShortToastCenter("登录失败");
            }
        });

    }


////        Button buttontest = (Button) findViewById(R.id.test);
////        buttontest.setOnClickListener(new OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                new Thread(new Runnable() {
////                    @Override
////                    public void run() {
////                        try {
////                            new FTP().openConnect();
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }).start();
////
////            }
////        });
//        //上传功能
//        //new FTP().uploadMultiFile为多文件上传
//        //new FTP().uploadSingleFile为单文件上传
//        Button buttonUpload = (Button) findViewById(R.id.button_upload);
//        buttonUpload.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        // 上传
//                        File file = new File("/mnt/sdcard/SCS/scs.db");
//                        try {
//
//                            //单文件上传
//                            new FTP().uploadSingleFile(file, "",new UploadProgressListener(){
//
//                                @Override
//                                public void onUploadProgress(String currentStep,long uploadSize,File file) {
//                                    // TODO Auto-generated method stub
//                                    Log.d(TAG, currentStep);
//                                    if(currentStep.equals(LoginActivity.FTP_UPLOAD_SUCCESS)){
//                                        Log.d(TAG, "-----shanchuan--successful");
//                                    } else if(currentStep.equals(LoginActivity.FTP_UPLOAD_LOADING)){
//                                        long fize = file.length();
//                                        float num = (float)uploadSize / (float)fize;
//                                        int result = (int)(num * 100);
//                                        Log.d(TAG, "-----shangchuan---"+result + "%");
//                                    }
//                                }
//                            });
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                    }
//                }).start();
//
//            }
//        });
//
//        //下载功能
//        Button buttonDown = (Button)findViewById(R.id.button_down);
//        buttonDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        // 下载
//                        try {
//
//                            //单文件下载
//                            new FTP().downloadSingleFile("/scs.db","/mnt/sdcard/download/","scs.db",new DownLoadProgressListener(){
//
//                                @Override
//                                public void onDownLoadProgress(String currentStep, long downProcess, File file) {
//                                    Log.d(TAG, currentStep);
//                                    if(currentStep.equals(LoginActivity.FTP_DOWN_SUCCESS)){
//                                        Log.d(TAG, "-----xiazai--successful");
//                                    } else if(currentStep.equals(LoginActivity.FTP_DOWN_LOADING)){
//                                        Log.d(TAG, "-----xiazai---"+downProcess + "%");
//                                    }
//                                }
//
//                            });
//
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                    }
//                }).start();
//
//            }
//        });
//
//        //删除功能
//        Button buttonDelete = (Button)findViewById(R.id.button_delete);
//        buttonDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        // 删除
//                        try {
//
//                            new FTP().deleteSingleFile("/scs.db",new DeleteFileProgressListener(){
//
//                                @Override
//                                public void onDeleteProgress(String currentStep) {
//                                    Log.d(TAG, currentStep);
//                                    if(currentStep.equals(LoginActivity.FTP_DELETEFILE_SUCCESS)){
//                                        Log.d(TAG, "-----shanchu--success");
//                                    } else if(currentStep.equals(LoginActivity.FTP_DELETEFILE_FAIL)){
//                                        Log.d(TAG, "-----shanchu--fail");
//                                    }
//                                }
//
//                            });
//
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//                    }
//                }).start();
//
//            }
//        });
//
}
