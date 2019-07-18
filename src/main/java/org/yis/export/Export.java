package org.yis.export;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author milu
 * @Description 导出
 * @createTime 2019年07月10日 17:50:00
 */
public class Export {

    private Logger logger = LogManager.getLogger(Export.class);

    private int key;
    private Map<String, Object> props;

    public Export(int key, Map<String, Object> props) {
        this.key = key;
        this.props = props;
    }

    public void listen() {
        logger.info("Syslog_Export is running...");
        switch (this.key){
            case 1:
                // file
                export2File(props);
                break;
            case 2:
                // kafka
                export2Kafka(props);
                break;
            case 3:
                // es
                export2ES(props);
                break;
            default:
                logger.error("Input error！！！");
                System.exit(-1);
        }
    }

    /**
     * 输出内容到ES
     * @param props
     */
    private void export2ES(Map<String, Object> props) {

    }

    /**
     * 输出内容到kafka
     * @param props
     */
    private void export2Kafka(Map<String, Object> props) {
        String url = (String) props.get("bootstrapServers");
        String topic = (String) props.get("topic");
        ExportKafka kafka = new ExportKafka(url, topic);
        kafka.write2Kafka();
    }

    /**
     * 输出内容到本地文本
     * @param props
     */
    private void export2File(Map<String, Object> props) {
        String path = (String) props.get("filePath");
        ExportFile file = new ExportFile();
        file.write2File(path);
    }

}
