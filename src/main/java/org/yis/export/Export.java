package org.yis.export;

import java.util.Map;

/**
 * @author milu
 * @Description 导出
 * @createTime 2019年07月10日 17:50:00
 */
public class Export {

    private int key;
    private Map<String, Object> props;

    public Export(int key, Map<String, Object> props) {
        this.key = key;
        this.props = props;
    }

    public void listen() {
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
                System.out.println("输入有误 ！！！");
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

    }

    /**
     * 输出内容到本地文本
     * @param props
     */
    private void export2File(Map<String, Object> props) {
        String path = (String) props.get("filePath");
        ExportFile file = new ExportFile();

    }

}
