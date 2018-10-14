package com.socket.ftpdome.entity;

import java.io.Serializable;

public class FTPEntity implements Serializable{
    /**
     * 服务器名.
     */
    public String hostName;

    /**
     * 端口号
     */
    public int serverPort;

    /**
     * 用户名.
     */
    public String userName;

    /**
     * 密码.
     */
    public String password;

}
