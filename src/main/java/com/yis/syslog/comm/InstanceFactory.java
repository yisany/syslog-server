package com.yis.syslog.comm;

import org.apache.commons.lang3.StringUtils;

/**
 * @author by yisany on 2020/05/13
 */
public class InstanceFactory {

    private static String point = ".";

    static {
        System.setProperty("input", "com.yis.syslog.input.inputs");
        System.setProperty("filter", "com.yis.syslog.filter,filters");
        System.setProperty("output", "com.yis.syslog.output.outputs");
    }

//    protected static Class<?> getPluginClass(String type, String pluginType, String className) {
//        /*
//        type: file
//        pluginType: output
//        className: com.yis.syslog.output.outputs.file.FileOutput
//         */
//        try {
//            String[] names = type.split("\\.");
//            String key = String.format("%s:%s", pluginType, names[names.length - 1].toLowerCase()); // output:file
//            return JarClassLoader.getClassLoaderByPluginName(key).loadClass(className);
//        }
//
//    }

    protected static String getClassName(String type, String pluginType) {
        String className = getRealClassName(type, pluginType);
        return className;
    }

    private static String getRealClassName(String name, String key) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (name.indexOf(point) >= 0) {
            return name;
        }
        return getSystemProperty(key) + point + name.toLowerCase() + point + getNameNotation(name);
    }

    private static String getNameNotation(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    private static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

}
