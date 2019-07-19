package org.yis.export;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.yis.entity.EsDocument;
import org.yis.entity.Message;
import org.yis.entity.MessageQueue;
import org.yis.util.RestClientUtils;

/**
 * @author milu
 * @Description 输出到ES
 * @createTime 2019年07月11日 15:08:00
 */
public class ExportES {

    private Logger logger = LogManager.getLogger(ExportES.class);

    private static ClientInterface client;

    static {
        client = ElasticSearchHelper.getRestClientUtil("esmapper/syslog.xml");
    }

    public void write2Es(String index) {
        logger.info("exportToKafka is working...");
        if (client.existIndice(index)) {
            // 创建索引
            client.createIndiceMapping(index, "createSyslogIndice");
        }
        // 写入es
        try {
            while (true) {
                String msg = MessageQueue.getInstance().take().toString();
                client.addDocument(index, "syslog", transportMsg(msg));
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.warn("ExportKafka.write2Kafka error, e={}", e);
        }
    }

    private EsDocument transportMsg(String msg) {
        EsDocument document = new EsDocument();
        Message message = JSONObject.toJavaObject((JSONObject) JSON.parse(msg), Message.class);
        document.setHost(message.getHost());
        document.setIpAddress(message.getIpAddress());
        document.setMessage(message.getMessage());
        document.setPort(message.getPort());
        document.setPri(message.getPri());
        document.setProcessName(message.getProcessName());
        document.setUnique(message.getUnique());
        document.setTimeStamp(toUTC(message.getTimeStamp(), "yyyy-MM-dd HH:mm:ss.SSS"));
        return document;
    }

    public static String toUTC(String time, String fromPattern) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fromPattern);
        return DateTime.parse(time, formatter).toDateTime(DateTimeZone.UTC).toString();
    }


}
