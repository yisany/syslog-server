package org.yis.core.syslog4j_syslog;

import org.productivity.java.syslog4j.server.SyslogServerIF;
import org.productivity.java.syslog4j.server.impl.net.tcp.ssl.SSLTCPNetSyslogServerConfig;

public class TLSSyslogServerConfig extends SSLTCPNetSyslogServerConfig {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends SyslogServerIF> getSyslogServerClass() {
		return TLSSyslogServer.class;
	}

}
