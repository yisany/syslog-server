package org.yis.entity;

import com.frameworkset.orm.annotation.ESIndex;
import lombok.Data;

/**
 * @author milu
 * @Description es_document
 * @createTime 2019年07月19日 16:11:00
 */
@Data
@ESIndex(name = "syslogIndex-{agentStarttime,yyyy.MM.dd}",type="syslogType")
public class EsDocument {
    private String ipAddress;
    private int port;
    private String unique;
    private int pri;
    private String timeStamp;
    private String host;
    private String processName;
    private String message;

}
