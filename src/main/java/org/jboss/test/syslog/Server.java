/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.syslog;

import java.net.UnknownHostException;

import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerConfigIF;

/**
 * Syslog server.
 *
 * @author Josef Cacek
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
