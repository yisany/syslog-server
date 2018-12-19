package org.yis;

import java.net.InetAddress;

/**
 * Aim: Message信息封装
 * Date: 2018/11/22 20:43
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Message {

    private InetAddress ipAddress;
    private int port;
    private String unique;
    private String pri;
    private String timeStamp;
    private String host;
    private String process;
    private String message;

    @Override
    public String toString() {
        return "Message{" +
                "ipAddress=" + ipAddress +
                ", port=" + port +
                ", unique='" + unique + '\'' +
                ", pri='" + pri + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", host='" + host + '\'' +
                ", process='" + process + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getPri() {
        return pri;
    }

    public void setPri(String pri) {
        this.pri = pri;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getmessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(InetAddress ipAddress, int port, String unique, String pri, String timeStamp, String host, String process, String message) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.unique = unique;
        this.pri = pri;
        this.timeStamp = timeStamp;
        this.host = host;
        this.process = process;
        this.message = message;
    }

    public Message() {
    }
}
