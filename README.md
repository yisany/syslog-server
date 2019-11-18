# Syslog日志接收服务器

该项目基于 [syslog4j](http://www.syslog4j.org/) 和 [simple-syslog-server](https://github.com/kwart/simple-syslog-server) 实现

有两个版本（默认使用Netty版本）：

- 基于 `syslog4j` jar包实现
- 基于Netty实现

有三种协议可供选择：

- UDP
- TCP
- TLS (使用自签名证书) 

udp监听9897端口，tcp监听9898端口，tls监听9899端口

<br>

## 项目结构

项目目前分为四个包和一个启动类：

```
syslog_server
  ├── Syslog.java  -- 启动类
  ├── core	-- 接收服务器
  ├── entity	-- 实体类
  ├── export	-- output类
  └── util	-- 工具类
```



## 使用方法

使用maven打包成jar包，然后运行：

```
java -jar simple-syslog-server.jar
```

<br>

## 发送方配置 Client

### 自动脚本配置

可以使用shell文件夹下的`syslog_template.sh`模板，填入自己的内容，会进行一系列的自检，通过后便会开始配置rsyslog服务

```bash
CONF_FILE="/etc/rsyslog.d/dtstack.conf"
# 填入接收服务器的地址
SYSLOG_SERVER_HOST=
PORT="9898"
# 填入协议方式：udp,tcp,tls
PROTOCOL=
# 填入server.crt文件所在路径
URL_PEM=
```

### 手动配置

#### udp和tcp

- 在`/etc/rsyslog.d/`下新建文件user.conf

  ```bash
  touch user.conf
  ```

- 编辑文件

  ```bash
  $template UniqueFormat,"unique %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%
  # ip为服务端ip，确保两台机器可以ping通
  # udp为一个@，tcp为两个@
  #*.*  @ip:9898;UniqueFormat
  *.*  @@ip:9898;UniqueFormat
  ```

### tls

tls需要完成一下几个步骤：

1. 安装 rsyslog-gnutls

   ```bash
   sudo yum install -y rsyslog-gnutls
   ```

2. 移动 server.crt 文件到指定位置

   ```bash
   mkdir /etc/rsyslog.d/pem
   mv server.crt /etc/rsyslog.d/pem
   ```

3. 新建 user.conf

   ```bash
   touch /etc/rsyslog.d/user.conf
   ```

4. 编辑文件

   ```bash
   $DefaultNetstreamDriver gtls
   
   $DefaultNetstreamDriverCAFile /etc/rsyslog.d/pem/server.crt
   
   $ActionSendStreamDriverAuthMode anon
   $ActionSendStreamDriverMode 1
   
   $template myFormat,"unique %syslogpriority% %timestamp% %hostname% %syslogtag% %msg%"
   *.* @@ip:9898;myFormat
   ```

最后手动重启`rsyslog`服务:

```
systemctl restart rsyslog
```

<br>

## License

- [GNU Lesser General Public License Version 2.1](