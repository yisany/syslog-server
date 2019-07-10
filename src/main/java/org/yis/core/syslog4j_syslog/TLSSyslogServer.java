package org.yis.core.syslog4j_syslog;

import org.apache.commons.io.IOUtils;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.yis.util.TrustEveryoneTrustManager;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * 用于TLC的服务
 *
 * @author Josef Cacek
 */
public class TLSSyslogServer extends TCPSyslogServer {

	private SSLContext sslContext;

	/**
	 * Creates custom sslContext from keystore and truststore configured in
	 *
	 * @see org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogServer#initialize()
	 */
	@Override
	public void initialize() throws SyslogRuntimeException {
		System.out.println("TLSSyslogServer.initalize");
		super.initialize();

		try {
			final KeyStore keystore = KeyStore.getInstance("JKS");
			final InputStream is = getClass().getResourceAsStream("/server.keystore");
			if (is == null) {
				System.err.println("Server keystore not found.");
			}
			final char[] keystorePwd = "123456".toCharArray();
			try {
				keystore.load(is, keystorePwd);
			} finally {
				IOUtils.closeQuietly(is);
			}

			final KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keystore, keystorePwd);

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new TrustEveryoneTrustManager() },
					null);
		} catch (Exception e) {
			System.err.println("Exception occured during SSLContext for TLS syslog server initialization");
			e.printStackTrace();
			throw new SyslogRuntimeException(e);
		}
	}

	/**
	 * Returns {@link ServerSocketFactory} from custom {@link SSLContext}
	 * instance created in {@link #initialize()} method.
	 *
	 * @see org.productivity.java.syslog4j.server.impl.net.tcp.TCPNetSyslogServer#getServerSocketFactory()
	 */
	@Override
	protected ServerSocketFactory getServerSocketFactory() throws IOException {
		return sslContext.getServerSocketFactory();
	}

}
