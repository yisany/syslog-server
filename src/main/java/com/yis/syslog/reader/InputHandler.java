package com.yis.syslog.reader;

import com.yis.syslog.reader.syslog.SyslogTCPInitializer;
import com.yis.syslog.reader.syslog.SyslogTLSInitializer;
import com.yis.syslog.reader.syslog.UDPMessageHandler;
import com.yis.syslog.domain.enums.ProtocolEnum;
import com.yis.syslog.util.TrustEveryoneTrustManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Aim: Netty版Server
 * Date: 18-12-18 15:39
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class InputHandler {

    private static final Logger logger = LogManager.getLogger(InputHandler.class);

    private int port;
    private ProtocolEnum protocol;

    private EventLoopGroup group;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private SSLContext sslContext;

    public InputHandler(int port, ProtocolEnum protocol) {
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * 监听服务
     */
    public void listen() {
        switch (protocol){
            case UDP:
                logger.info("Syslog_UDP_Monitor is running...");
                udp(port);
                break;
            case TCP:
                logger.info("Syslog_TCP_Monitor is running...");
                tcp(port);
                break;
            case TLS:
                logger.info("Syslog_TLS_Monitor is running...");
                SSLContext sslContext = getSslContext();
                JdkSslContext context = new JdkSslContext(sslContext, false, ClientAuth.NONE);
                tls(port, context);
                break;
            default:
                logger.info("Input error !!!");
                System.exit(-1);
        }
    }

    /**
     * tls证书认证
     * @return
     */
    private SSLContext getSslContext(){
        try {
            // keystore的类型，默认是jks
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final InputStream is = getClass().getResourceAsStream("/server.keystore");
            if (is == null){
                logger.error("InputHandler keystore not found.");
            }
            final char[] keystorePwd = "123456".toCharArray();
            try {
                keyStore.load(is,keystorePwd);
            } finally {
                IOUtils.closeQuietly(is);
            }
            // 创建jkd密钥访问库    123456是keystore密码
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.
                    getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePwd);

            // 构造SSL环境，指定SSL版本为TLS
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(),
                    new TrustManager[] { new TrustEveryoneTrustManager() }, null);
        } catch (Exception  e) {
            logger.error("InputHandler.getSslContext warning, e={}", e);
        }
        return sslContext;
    }

    /**
     * udp
     * @param port
     */
    private void udp(int port) {
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioDatagramChannel.class);
            b.handler(new UDPMessageHandler());
            b.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            logger.error("InputHandler.udp warning, e={}", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * tcp
     * @param port
     */
    private void tcp(int port){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SyslogTCPInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("InputHandler.tcp warning, e={}", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * tls
     * @param port
     */
    private void tls(int port, JdkSslContext context) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new SyslogTLSInitializer(context))
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("InputHandler.tls warning, e={}", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
