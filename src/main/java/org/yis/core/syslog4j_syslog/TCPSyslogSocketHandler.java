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
package org.yis.core.syslog4j_syslog;

import org.apache.commons.io.IOUtils;
import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.yis.entity.Message;
import org.yis.export.Export;
import org.yis.util.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Set;

/**
 * 用于TCP和TLS服务的Socket处理
 */
public class TCPSyslogSocketHandler implements Runnable {

	protected SyslogServerIF server = null;
	protected Socket socket = null;
	protected Set<Socket> sockets = null;

	/**
	 * Constructor.
	 *
	 * @param sockets
	 *            Set of all registered handlers.
	 * @param server
	 *            Syslog server instance
	 * @param socket
	 *            socket returned from the serverSocket.accept()
	 */
	public TCPSyslogSocketHandler(Set<Socket> sockets, SyslogServerIF server, Socket socket) {
		this.sockets = sockets;
		this.server = server;
		this.socket = socket;

		synchronized (this.sockets) {
			this.sockets.add(this.socket);
		}
	}

	public void run() {
		try {
			System.out.println("进入tcp SyslogServer");
			final BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
			int b = -1;
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			boolean firstByte = true;
			boolean octetCounting = false;
			StringBuilder octetLenStr = new StringBuilder();
			do {
				b = bis.read();
				if (firstByte && b >= '1' && b <= '9') {
					// handle Octet Counting messages (cf. rfc-6587)
					octetCounting = true;
				}
				firstByte = false;
				if (octetCounting) {
					if (b != ' ') {
						octetLenStr.append((char) b);
					} else {
						int len = Integer.parseInt(octetLenStr.toString());
						handleSyslogMessage(IOUtils.toByteArray(bis, len));
						// reset the stuff
						octetLenStr = new StringBuilder();
						firstByte = true;
						octetCounting = false;
					}
				} else {
					// handle Non-Transparent-Framing messages (cf. rfc-6587)
					switch (b) {
					case -1:
					case '\r':
					case '\n':
						if (baos.size() > 0) {
							handleSyslogMessage(baos.toByteArray());
							baos.reset();
							firstByte = true;
						}
						break;
					default:
						baos.write(b);
						break;
					}
				}
			} while (b != -1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket);
			sockets.remove(socket);
		}
	}

	private void handleSyslogMessage(final byte[] rawmessage) {
//		final SyslogServerEventIF event = new Rfc5424SyslogEvent(rawmessage, 0, rawmessage.length);
//		System.out.println(">>> Syslog message came: " + event);

		//DatagramPacket：此类表示数据报包
		final DatagramPacket dp = new DatagramPacket(rawmessage, rawmessage.length);
		//传入一个数据报包，用来接收数据
		String mess = new String(dp.getData(),0, dp.getLength());
		//信息初始化
		Message message = Utils.initMessage(this.socket.getInetAddress(), this.socket.getPort(), mess);
		System.out.println(">>> message came: "+ message);

		//加入到jlogstash-input还要置入Input内存队列
		Export.pushToOut(message);
	}
}