package org.jboss.test.syslog;

import java.net.UnknownHostException;

import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;

/**
 * Aim:  Server服务主入口
 * Date: 2018/11/23 9:48
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Server {

	public static final int SYSLOG_PORT = 9898;

	public static void main(String[] args) throws SyslogRuntimeException, UnknownHostException {

		System.out.println("调用Input-syslog插件");
		SyslogServer.shutdown();

		//设置以哪种方式接收，默认为udp
		Protocol protocol = Protocol.name("udp");

		//配置初始化
		SyslogServerConfigIF configIF = ConfigBuilder.newBuilder(protocol).build();

		if (configIF == null) {
			System.out.println("Unsupported Syslog protocol: " + configIF);
		}
		//设置参数
		configIF.setUseStructuredData(true);
		configIF.setHost("0.0.0.0");
		configIF.setPort(SYSLOG_PORT);

		/**
		 * 启动syslog服务器
		 * syslogProtocol：协议名称（udp、tcp、tls）
		 * configIF：配置类信息
		 */
		SyslogServer.createThreadedInstance(protocol.getName(), configIF);
	}

}
