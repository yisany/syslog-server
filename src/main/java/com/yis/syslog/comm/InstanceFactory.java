package com.yis.syslog.comm;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author by yisany on 2020/05/13
 */
public class InstanceFactory {

    private static String point = ".";

    private static InstanceFactory factory;

    static {
        factory = new InstanceFactory();

        System.setProperty("input", "com.yis.syslog.input.inputs");
        System.setProperty("filter", "com.yis.syslog.filter,filters");
        System.setProperty("output", "com.yis.syslog.output.outputs");
    }

    protected static Class<?> getPluginClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = factory.getClass().getClassLoader();
        return classLoader.loadClass(className);
    }

    protected static void configInstance(Object instance, Map<String, Object> config) throws IllegalAccessException {
        Field[] fields =instance.getClass().getDeclaredFields();
        if (!config.isEmpty()) {
            for (Field field : fields) {
                field.setAccessible(true);
                // 判断非静态变量
                if((field.getModifiers() & java.lang.reflect.Modifier.STATIC) != java.lang.reflect.Modifier.STATIC) {
                    String name = field.getName();
                    Object obj = config.get(name);
                    if (ObjectUtils.anyNotNull(obj)) {
                        field.set(instance, obj);
                    }
                }
            }
        }
    }

    protected static String getClassName(String type, String pluginType) {
        String className = getRealClassName(type, pluginType);
        return className;
    }

    private static String getRealClassName(String name, String key) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (name.contains(point)) {
            return name;
        }
        return getSystemProperty(key) + point + name.toLowerCase() + point + getNameNotation(name, key);
    }

    private static String getNameNotation(String name, String key) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        char[] keyCs = key.toCharArray();
        keyCs[0] -= 32;
        return String.valueOf(cs) + String.valueOf(keyCs);
    }

    private static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

}
