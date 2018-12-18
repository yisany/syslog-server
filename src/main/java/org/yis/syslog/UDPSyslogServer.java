/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.yis.syslog;

import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.server.impl.net.udp.UDPNetSyslogServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 * 用于UDP服务处理
 */
public class UDPSyslogServer extends UDPNetSyslogServer {

	@Override
	public void shutdown() {
		/*
		Thread.sleep((long)this.syslogServerConfig.getShutdownWait());
		Thread.sleep(500);
		 */
		super.shutdown();
		thread = null;
	}

	@Override
	public void run() {
		System.out.println("调用UDPSyslogServer");
		this.shutdown = false;
		try {
			/**
			 * ds是一个DatagramSocket的实例类
			 * DatagramSocket：用来发送和接收数据报包的套接字
			 */
			this.ds = createDatagramSocket();
		} catch (Exception e) {
			System.err.println("Creating DatagramSocket failed");
			e.printStackTrace();
			throw new SyslogRuntimeException(e);
		}

		//新建一个接收缓冲区，1024byte
		byte[] receiveData = new byte[SyslogConstants.SYSLOG_BUFFER_SIZE];

		while (!this.shutdown) {
			try {
				//DatagramPacket：此类表示数据报包
				final DatagramPacket dp = new DatagramPacket(receiveData, receiveData.length);
				//传入一个数据报包，用来接收数据
				this.ds.receive(dp);
				String mess = new String(dp.getData(),0, dp.getLength());
				//System.out.println(">>> init mess came: "+ mess);
				//信息初始化
				Message message = Utils.initMessage(dp.getAddress(), dp.getPort(), mess);
				System.out.println(">>> message came: "+ message.toString());

				//加入到jlogstash-input还要置入Input内存队列
				Utils.pushToInput(message);
			} catch (SocketException se) {
				se.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}
