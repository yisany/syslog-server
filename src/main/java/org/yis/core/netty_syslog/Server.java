package org.yis.core.netty_syslog;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import org.apache.commons.io.IOUtils;
import org.yis.util.TrustEveryoneTrustManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
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
public class Server {

    private int port;
    private String protocol;

    private EventLoopGroup group;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private SSLContext sslContext;

    public Server(int port, String protocol) {
        this.port = port;
        this.protocol = protocol;
    }

    /**
     * 监听服务
     */
    public void listen() {
        switch (protocol.toLowerCase()){
            case "udp":
                udp(port);
                break;
            case "tcp":
                tcp(port);
                break;
            case "tls":
                SSLContext sslContext = getSslContext();
                JdkSslContext context = new JdkSslContext(sslContext, false, ClientAuth.NONE);
                tls(port, context);
                break;
            default:
                System.out.println("输入有误 ！！！");
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
                System.err.println("Server keystore not found.");
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private class SyslogTCPInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new TCPMessageHandler());
        }
    }

    private class SyslogTLSInitializer extends ChannelInitializer<SocketChannel> {

        private JdkSslContext context;

        public SyslogTLSInitializer(JdkSslContext context) {
            this.context = context;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            SSLEngine sslEngine = context.newEngine(ch.alloc());
            ch.pipeline().addFirst("ssl", new SslHandler(sslEngine));

            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new TCPMessageHandler());

        }
    }

}
