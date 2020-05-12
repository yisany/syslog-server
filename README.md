# Syslog Server

该项目实现了一个syslog日志服务器, 可以接收服务器和网络设备以syslog服务发送的日志信息,并可以将日志进行解析后进行数据的转发.

目前支持三种协议：

> UDP, TCP, TLS (使用自签名证书) 

解析协议:

> RFC_5424, RFC_3164, UNKNOWN(不解析, 原文发送)

转发方式:

>stdout(控制台打印), file(文件落盘), kafka

<br>

## Usage

使用maven打包成jar包，然后运行：

```
mvn clean package -Dmaven.test.skip=true
java -jar syslog-server.jar -c /tmp/application.yaml
```

<br>

## Server config

启动支持参数

```
-f	配置文件 yaml格式路径
```

<br>

## Client config

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
  # ip为服务端ip，确保两台机器可以ping通
  # udp为一个@，tcp为两个@
  #*.*  @ip:9898
  *.*  @@ip:9898
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
   
   *.* @@ip:9898
   ```

最后手动重启`rsyslog`服务:

```
systemctl restart rsyslog
```

<br>

## UPDATE

20200511:

分支: `dev_v3.1.1`

更新功能:

1. 新增解析模块, 支持`RFC_5424`, `RFC_3164`, `UNKNOWN`三种模式解析日志
2. 更改数据传输队列, 使用队列集合, 每个队列对应一组处理器, 接收队列和发送队列分离, 避免生产消费吞吐量不一致导致卡死的问题.
3. 定制input, output模板, 需要新增接收/发送器只需要实现相关接口即可
   - com.yis.syslog.input.Input
   - com.yis.syslog.output.Output
4. 发送器目前支持: `file`, `kafka2.3.0`, `stdout`

20200107:

1. 去除`syslog4j`模组
2. 配置文件从properties改为yaml, 协议接收端口可配
3. 输出端提供文件落盘和kafka导出两种方式

<br>

## License

- [GNU Lesser General Public License Version 2.1]