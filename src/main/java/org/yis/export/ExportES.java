package org.yis.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.util.RestClientUtils;

/**
 * @author milu
 * @Description 输出到ES
 * @createTime 2019年07月11日 15:08:00
 */
public class ExportES {

    private Logger logger = LogManager.getLogger(ExportES.class);

    private String host;
    private int port;

    private RestClientUtils utils;

    public ExportES(String host, int port) {
        this.host = host;
        this.port = port;
        utils = new RestClientUtils(host, port, RestClientUtils.Scheme.HTTP.getProtocol());
    }

    public void write2Es() {

    }


}
