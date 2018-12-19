package org.yis.netty_syslog;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.io.IOUtils;
import org.yis.TrustEveryoneTrustManager;

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
    private SSLEngine sslEngine;
    //是否存在ssl
    private boolean sslEnable = false;

    public Server(int port, String protocol) {
        this.port = port;
        this.protocol = protocol;
        //this.sslContext = getSslContext();
    }

    /**
     * 监听服务
     */
    public void listen() {
        System.out.println("Server: " + protocol);
        if (protocol.equalsIgnoreCase("udp")){
            // 调用udp服务
            System.out.println("This is Syslog.udp");
            udp(port);
        } else if (protocol.equalsIgnoreCase("tcp")){
            // 调用tcp服务
            System.out.println("This is Syslog.tcp");
            tcp(port);
        } else if(protocol.equalsIgnoreCase("tls")){
            // 调用tls服务
            System.out.println("This is Syslog.tls");
            SSLContext sslContext = getSslContext();

            // 将SslEnable设置为true
            if (this.sslContext != null){
                sslEnable = true;
            }

            sslEngine = sslContext.createSSLEngine();
            // 是否使用客户端模式
            sslEngine.setUseClientMode(false);
            // 是否需要验证客户端
            sslEngine.setNeedClientAuth(false);

            tcp(port);
        } else {
            System.out.println("输入有误 ！！！");
            System.exit(-1);
        }
    }

    private boolean isSslEnable(){
        return this.sslEnable;
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
                    .childHandler(new SyslogTCPInitializer(this ))
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


    private class SyslogTCPInitializer extends ChannelInitializer<SocketChannel> {

        private Server server;

        private SslContext context;

        public SyslogTCPInitializer(Server server) {
            this.server = server;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            //判断是否存在ssl
            if (server.isSslEnable()){
//                SSLEngine engine = context.newEngine(ch.alloc());
//                engine.setUseClientMode(false);
//                ch.pipeline().addFirst(new SslHandler(engine));
                ch.pipeline().addFirst(new SslHandler(server.sslEngine));
            }

            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
            ch.pipeline().addLast(new StringDecoder());
            ch.pipeline().addLast(new TCPMessageHandler());
        }
    }

}
