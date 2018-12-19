package org.yis;

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
     * @param mess 日志信息
     * @return message
     */
    public static Message initMessage(InetAddress address, int port, String mess) {
        Message message = new Message();
        message.setIpAddress(address);
        message.setPort(port);

        //消息message分割
        String[] str = mess.split(" ");
        //设置unique
        message.setUnique(str[0]);
        //设置优先级
        message.setPri(str[1]);
        /**
         * 这里有一个小地方需要注意：
         * 日期如果为两位数，如11，28，切分正确
         * 如果为一位数，如6，2，会多出来一个空格
         */
        if ("".equals(str[3])){
            for (int i = 3; i < str.length - 1; i++){
                str[i] = str[i + 1];
            }
        }
        //设置时间戳
        StringBuffer buffer = new StringBuffer();
        buffer.append(str[2]).append(" ").append(str[3]).append(" ").append(str[4]);
        message.setTimeStamp(buffer.toString());
        //设置主机名
        message.setHost(str[5]);
        //设置应用名
        message.setProcess(str[6]);
        //设置message
        buffer = new StringBuffer();
        for (int i = 7; i < str.length; i ++){
            if (i != str.length){
                buffer.append(str[i]).append(" ");
            }else {
                buffer.append(str[i]);
            }
        }
        message.setMessage(buffer.toString());
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
