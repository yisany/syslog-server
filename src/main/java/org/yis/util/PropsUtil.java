package org.yis.util;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author milu
 * @Description 配置文件读取
 * @createTime 2019年07月10日 14:57:00
 */
public class PropsUtil {

    private static volatile ConcurrentHashMap<String, Object> props = new ConcurrentHashMap<>();

    private Properties pro = new Properties();

    private Properties readProps()  {
        Properties properties = new Properties();
        try {
            final InputStream in = getClass().getResourceAsStream("/application.properties");
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void props2Maps() {
        pro = readProps();
        Iterator<String> it = pro.stringPropertyNames().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = pro.getProperty(key);
            judgeValueType(key, value);
        }
    }

    /**
     * 判断数据格式
     * 会有三种格式：str,list,map
     * @param value
     */
    private void judgeValueType(String key, String value) {
        if (value.trim().startsWith("{")) {
            Map<String, Object> tmp = JSON.parseObject(value, Map.class);
            props.put(key, tmp);
        } else if (value.trim().startsWith("[")) {
            List<Object> tmp = JSON.parseObject(value, List.class);
            props.put(key, tmp);
        } else {
            props.put(key, value);
        }
    }

    public static Map getProps() {
        PropsUtil util = new PropsUtil();
        if (props.size() == 0) {
            util.props2Maps();
        }
        return props;
    }

}
