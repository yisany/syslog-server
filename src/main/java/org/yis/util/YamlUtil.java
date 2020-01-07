package org.yis.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author milu
 * @Description yaml解析
 * @createTime 2020年01月03日 11:38:00
 */
public class YamlUtil {

    private static final Logger logger = LogManager.getLogger(YamlUtil.class);

    private static File yamlFile;
    private static InputStream is;
    private static Yaml yaml;

    /**
     * 解析yaml文件
     * @param configStr 配置文件地址
     * @return
     */
    public static Map<String, Object> parseYaml(String configStr) {
        Map<String, Object> yamlMap;
        yamlFile = new File(configStr);
        try {
            if (yamlFile.exists()) {
                is = new FileInputStream(yamlFile);
                yaml = new Yaml();
                yamlMap = yaml.load(is);
                return yamlMap;
            } else {
                logger.error("File not exist: {}", configStr);
                throw new BizException("File not exist");
            }
        } catch (FileNotFoundException e) {
            logger.error("File not exist: {}", configStr);
            throw new BizException("File not exist");
        }

    }

}
