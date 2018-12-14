# Syslog日志接收服务器

该项目基于 [syslog4j](http://www.syslog4j.org/) 实现

有三种协议可供选择：

 * UDP
 * TCP
 * TLS (使用自签名证书) 

Syslog服务器监听 **port 9898** 端口

## 使用方法

使用maven打包成jar包，然后运行：

    java -jar simple-syslog-server.jar [udp|tcp|tls]

如果未提供后面的协议参数，默认使用 **"udp"** 启动监听。

## 发送方配置

```bash
#! /bin/bash

cp /etc/rsyslog.conf /etc/rsyslog.conf.backup99

file="/etc/rsyslog.conf"
# 服务端ip
address=""192.168.92.1""
# 接收端口
port="9898"
# 协议方式
protocol=""@""
# 自定义模板
content1="\$template UniqueFormat,\"test %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%\""
content2="*.*  ${protocol}${address}:${port};UniqueFormat"

echo "$content1" >> $file
echo "$content2" >> $file

systemctl restart rsyslog.service


```

上面是一个`rsyslog.conf`的配置脚本，可以自己修改内容。后面会再开放出一个专门的服务，可以用于自动生成脚本。

## License

* [GNU Lesser General Public License Version 2.1](http://www.gnu.org/licenses/lgpl-2.1-standalone.html)
