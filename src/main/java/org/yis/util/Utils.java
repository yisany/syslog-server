package org.yis.util;

import com.alibaba.fastjson.JSON;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.yis.entity.Const;
import org.yis.entity.Message;
import org.yis.entity.MessageQueue;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Aim: 工具类
 * Date: 2018/11/23 10:59
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class Utils {

    private static String year = "";

    /**
     * 日志消息使用Message进行封装
     *
     * @param address ip地址
     * @param port    端口号
     * @param msg     日志信息
     * @return message
     */
    public static Message initMessage(InetAddress address, int port, String msg) {
        Message message = new Message();
        message.setIpAddress(address.toString().substring(1));
        message.setPort(port);
        Matcher m = Const.SYSLOG_REGEX.matcher(msg);
        if (m.find()) {
            String[] str = msg.split("\\s+");
            message.setUnique(str[0]);
            message.setPri(Integer.parseInt(str[1]));
            parseDayAndTime(str, message);
            message.setHost(str[5]);
            message.setProcessName(str[6].substring(0, str[6].length() - 1));
            message.setMessage(structureMsg(msg));
        } else {
            message.setUnique("unknown");
            message.setPri(0);
            message.setTimeStamp(getTime());
            message.setHost("unknown");
            message.setProcessName("unknown");
            message.setMessage(structureMsg(msg));
        }
        return message;
    }

    /**
     * 去除unique生成消息正文
     * @param msg
     * @return
     */
    private static String structureMsg(String msg) {
        Matcher matcher = Const.SYSLOG_MAIN_BODY.matcher(msg);
        if (matcher.find()) {
            return matcher.group();
        }
        return msg;
    }

    /**
     * 解析时间
     *
     * @param str
     * @param message
     */
    private static void parseDayAndTime(String[] str, Message message) {
        // 先解析时间
        String day = null;
        if (str[3].length() == 1) {
            // 1 - 9
            day = Const.MONTH.get(str[2]) + " 0" + str[3];
        } else {
            // 10 - 31
            day = Const.MONTH.get(str[2]) + " " + str[3];
        }
        LocalDate localDate = LocalDate.parse((year == "" ? getCurrentYear() : year) + " " + day, Const.DATE_TIME_FORMATTER);
        message.setTimeStamp(localDate.toString() + " " + str[4]);
    }

    /**
     * 获取系统年份
     *
     * @return
     */
    public static String getCurrentYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        year = sdf.format(date);
        return sdf.format(date);
    }

    /**
     * 获取日期
     * @return
     */
    public static String getDate() {
        Date date = new Date();
        DateFormat short0 = DateFormat.getDateInstance( );
        return short0.format(date);
    }

    /**
     * 获取时间
     * @return
     */
    public static String getTime() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String s = DateTime.parse(time.toString(), formatter).toDateTime(DateTimeZone.UTC).toString();
        return s;
    }

    /**
     * 转换UTC时间
     * @param time
     * @param fromPattern
     * @return
     */
    public static String toUTC(String time, String fromPattern) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(fromPattern);
        return DateTime.parse(time, formatter).toDateTime(DateTimeZone.UTC).toString();
    }

    /**
     * 置入内存队列
     *
     * @param message
     */
    public static void pushToInput(Message message) {
        try {
            MessageQueue.getInstance().put(JSON.toJSONString(message));
//            System.out.println("队列中信息数量：" + MessageQueue.getInstance().size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * javaBean 转 Map
     *
     * @param object 需要转换的javabean
     * @return 转换结果map
     * @throws Exception
     */
    public static Map<String, Object> beanToMap(Object object) throws Exception {
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
     *
     * @param map 需要转换的map
     * @param cls 目标javaBean的类对象
     * @return 目标类object
     * @throws Exception
     */
    public static Object mapToBean(Map<String, Object> map, Class cls) throws Exception {
        Object object = cls.newInstance();
        for (String key : map.keySet()) {
            Field temFiels = cls.getDeclaredField(key);
            temFiels.setAccessible(true);
            temFiels.set(object, map.get(key));
        }
        return object;
    }

}
