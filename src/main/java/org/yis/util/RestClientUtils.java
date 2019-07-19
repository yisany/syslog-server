package org.yis.util;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author milu
 * @Description es 连接
 * @createTime 2019年07月19日 14:16:00
 */
public class RestClientUtils {

    private Logger logger = LogManager.getLogger(RestClientUtils.class);

    /**
     * 高阶Rest Client
     */
    private RestHighLevelClient client = null;
    /**
     * 低阶Rest Client
     */
    private RestClient restClient = null;

    private String host;
    private int port;
    private String scheme;

    public enum Scheme {
        HTTP("http"), HTTPS("https");

        private String protocol;

        Scheme(String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return this.protocol;
        }
    }

    /**
     * 这里使用饿汉单例模式创建RestHighLevelClient
     */
    public RestClientUtils(String host, int port, String scheme) {
        if (client == null) {
            synchronized (RestHighLevelClient.class) {
                if (client == null) {
                    client = getClient(host, port, scheme);
                }
            }
        }
    }

    private RestHighLevelClient getClient(String url, int port, String scheme) {
        RestHighLevelClient client = null;

        List<String> hostList = new ArrayList<>();

        if (url.contains(",")) {
            hostList = Arrays.asList(url.substring(1, url.length() - 1).split(","));
        }

        ArrayList<HttpHost> hosts = new ArrayList<>();

        hostList.stream().forEach(host -> {
            String[] hp = host.split(":");
            String h = null, p = null;
            if (hp.length == 2) {
                h = hp[0];
                p = hp[1];
            } else if (hp.length == 1) {
                h = hp[0];
                p = "9300";
            }
            hosts.add(new HttpHost(h, Integer.parseInt(p), scheme));
        });

        try {
            client = new RestHighLevelClient(
                    RestClient.builder(
                            hosts.toArray(new HttpHost[hosts.size()])
                    )
            );
        } catch (Exception e) {
            logger.warn("RestClientUtils.getClient error, e={}", e);
        }
        return client;
    }

    private RestClient getRestClient(String host, int port, String scheme) {
        RestClient client = null;

        try {
            client = RestClient.builder(
                    new HttpHost(host, port, scheme)
            ).build();
        } catch (Exception e) {
            logger.warn("RestClientUtils.getRestClient error, e={}", e);
        }
        return client;
    }

    public void closeClient() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.warn("RestClientUtils.closeClient error, e={}", e);
        }
    }

    /**
     * document API 主要是些简单的增删改查操作
     */
    public void documentAPI() {
        //...
    }

    /**
     * Search API 主要是些复杂查询操作
     */
    public void searchAPI() {
        //...
    }

    /**
     * 查询索引是否存在
     * @param index 索引名
     * @return
     */
    public boolean indexIsExist(String index) {
        return true;
    }
}
