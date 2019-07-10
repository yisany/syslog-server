package org.yis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.omg.CORBA.OBJ_ADAPTER;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author milu
 * @Description 配置文件读取
 * @createTime 2019年07月10日 14:57:00
 */
public class PropsUtil {

    private volatile ConcurrentHashMap<String, Object> props = new ConcurrentHashMap<>();

    private Properties pro = new Properties();

    private void readProps()  {
        try {
            final InputStream in = getClass().getResourceAsStream("/application.properties");
            pro.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void props2Maps() {
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

    public Map getProps() {
        return this.props;
    }

}
