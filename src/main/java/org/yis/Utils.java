package org.yis;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aim: 工具类
 * Date: 2018/11/23 10:59
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Utils {



    /**
     * 日志消息使用Message进行封装
     * @param address ip地址
     * @param port 端口号
     * @param msg 日志信息
     * @return message
     */
    public static Message initMessage(InetAddress address, int port, String msg) {
        Message message = new Message();
        message.setIpAddress(address.toString().substring(1));
        message.setPort(port);
        Matcher m = Const.getRegex().matcher(msg);
        if (m.find()) {
            String[] str = msg.split("\\s+");
            message.setUnique(str[0]);
            message.setPri(Integer.parseInt(str[1]));
            // TODO 日期转换
            StringBuffer buffer = new StringBuffer();
            buffer.append(str[2]).append(" ").append(str[3]).append(" ").append(str[4]);
            message.setTimeStamp(buffer.toString());
            message.setHost(str[5]);
            message.setProcessName(str[6].substring(0, str[6].length() - 1));
            message.setMessage(msg);
        } else {
            message.setUnique("unknown");
            message.setPri(0);
            message.setTimeStamp("unknown");
            message.setHost("unknown");
            message.setProcessName("unknown");
            message.setMessage(msg);
        }
        return message;
    }

    /**
     * 置入内存队列
     * 原来这是开发给logstash使用，现在抽离为一个单独的组件，所以注释掉
     * @param message
     */
    public static void pushToInput(Message message){
//        try {
//            String jsonObj = mapper.writeValueAsString(message);
//            Map<String, Object> event = Channel.getServer().getDecoder().decode(message.toString());
//            Map<String, Object> event = beanToMap(message);
//            System.out.println(event);
//            if (event != null && event.size() > 0) {
//                Channel.getServer().process(event);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error(e.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error(e.toString());
//        }

    }

    /**
     * javaBean 转 Map
     * @param object 需要转换的javabean
     * @return  转换结果map
     * @throws Exception
     */
    public static Map<String, Object> beanToMap(Object object) throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();

        Class cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(object));
        }
        return map;
    }

    /**
     * map转换为javaBean
     * @param map   需要转换的map
     * @param cls   目标javaBean的类对象
     * @return  目标类object
     * @throws Exception
     */
    public static Object mapToBean(Map<String, Object> map, Class cls) throws Exception
    {
        Object object = cls.newInstance();
        for (String key : map.keySet()){
            Field temFiels = cls.getDeclaredField(key);
            temFiels.setAccessible(true);
            temFiels.set(object, map.get(key));
        }
        return object;
    }

}
