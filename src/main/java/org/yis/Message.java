package org.yis;

/**
 * Aim: Message信息封装
 * Date: 2018/11/22 20:43
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Message {

    private String ipAddress;
    private int port;
    private String unique;
    private int pri;
    private String timeStamp;
    private String host;
    private String processName;
    private String message;

    @Override
    public String toString() {
        return "Message{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", unique='" + unique + '\'' +
                ", pri=" + pri +
                ", timeStamp='" + timeStamp + '\'' +
                ", host='" + host + '\'' +
                ", processName='" + processName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
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

    public int getPri() {
        return pri;
    }

    public void setPri(int pri) {
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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String ipAddress, int port, String unique, int pri, String timeStamp, String host, String processName, String message) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.unique = unique;
        this.pri = pri;
        this.timeStamp = timeStamp;
        this.host = host;
        this.processName = processName;
        this.message = message;
    }

    public Message() {
    }
}
