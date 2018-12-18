package org.yis.syslog;

import org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogServer;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;

/**
 * Aim:  关于TCP的Syslog4j服务
 * Date: 2018/11/23 9:48
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class TCPSyslogServer extends TCPNetSyslogServer {

	@SuppressWarnings("unchecked")
	public TCPSyslogServer() {
		sockets = Collections.synchronizedSet(sockets);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			System.out.println("Creating Syslog server socket");
			this.serverSocket = createServerSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (!this.shutdown) {
			try {
				final Socket socket = this.serverSocket.accept();
				System.out.println("Handling Syslog client " + socket.getInetAddress());
				new Thread(new TCPSyslogSocketHandler(this.sockets, this, socket)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
