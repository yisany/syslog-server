# Syslog日志接收服务器

该项目基于 [syslog4j](http://www.syslog4j.org/) 和 [simple-syslog-server](https://github.com/kwart/simple-syslog-server) 实现

有两个版本：

- 基于 `syslog4j` jar包实现
- 基于Netty实现

有三种协议可供选择：

- UDP
- TCP
- TLS (使用自签名证书) 

Syslog服务端监听 **port 9898** 端口

<br>

## 使用方法

使用maven打包成jar包，然后运行：

```
java -jar simple-syslog-server.jar [udp|tcp|tls]
```

如果未提供后面的协议参数，默认使用 **"udp"** 启动监听。

<br>

## 发送方配置

#### udp和tcp

下面是一个`rsyslog.conf`的配置脚本，可以自己修改内容。后面会再开放出一个专门的服务，可以用于自动生成脚本。

```bash
#! /bin/bash

file="/etc/rsyslog.d/user.conf"
# 填写你自己的服务端ip
address="192.168.92.1"
port="9898"
# udp为@，tcp为@@
protocol="@"
# 这边定义的是发送的格式，可以根据你自己的需求来定义
content1="\$template UniqueFormat,\"unique %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%\""
content2="*.*  ${protocol}${address}:${port};UniqueFormat"

touch $file

echo "$content1" >> $file
echo "$content2" >> $file

systemctl restart rsyslog.service

```

### tls

tls需要完成一下几个步骤：

1. 安装 rsyslog-gnutls

   ```bash
   sudo yum install -y rsyslog-gnutls
   ```

2. 生成.pem文件，server.crt在项目中可以找到

   ```bash
   cp server.crt /data
   cd /data
   openssl x509 -inform DER -in yourdownloaded.crt -out outcert.pem -text
   ```

3. 配置 rsyslog.conf 

   ```bash
   $DefaultNetstreamDriver gtls
   
   $DefaultNetstreamDriverCAFile /data/ca.pem
   $DefaultNetstreamDriverCertFile /data/outcert.pem
   $DefaultNetstreamDriverKeyFile /data/ca-key.pem
   
   $ActionSendStreamDriverAuthMode anon
   $ActionSendStreamDriverMode 1
   
   $template myFormat,"unique %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%"
   *.* @@192.168.92.1:9898;myFormat
   ```

<br>

## License

- [GNU Lesser General Public License Version 2.1](http://www.gnu.org/licenses/lgpl-2.1-standalone.html)