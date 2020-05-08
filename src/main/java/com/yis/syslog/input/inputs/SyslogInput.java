package com.yis.syslog.input.inputs;

import com.yis.syslog.domain.enums.ProtocolEnum;
import com.yis.syslog.domain.qlist.InputQueueList;
import com.yis.syslog.input.Input;
import com.yis.syslog.util.DateUtil;
import com.yis.syslog.comm.TrustEveryoneTrustManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Aim: Netty版Server
 * Date: 18-12-18 15:39
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class SyslogInput implements Input {

    private static final Logger logger = LogManager.getLogger(SyslogInput.class);

    private int port;
    private ProtocolEnum protocol;

    private EventLoopGroup group;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private SSLContext sslContext;
    private JdkSslContext context;

    private static InputQueueList inputQueueList;

    public SyslogInput(int port, ProtocolEnum protocol) {
        this.port = port;
        this.protocol = protocol;

        prepare();
    }

    public int getPort() {
        return port;
    }

    public ProtocolEnum getProtocol() {
        return protocol;
    }

    public static void setInputQueueList(InputQueueList inputQueueList) {
        SyslogInput.inputQueueList = inputQueueList;
    }

    @Override
    public void prepare() {
        if (ProtocolEnum.TLS == protocol) {
            SSLContext sslContext = getSslContext();
            context = new JdkSslContext(sslContext, false, ClientAuth.NONE);
        }
    }

    @Override
    public void emit() {
        switch (protocol) {
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
                tls(port, context);
                break;
            default:
                logger.info("Input error !!!");
                System.exit(-1);
        }
    }

    @Override
    public void release() {
        if (group != null) {
            group.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * tls证书认证
     *
     * @return
     */
    private SSLContext getSslContext() {
        try {
            // keystore的类型，默认是jks
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final InputStream is = getClass().getResourceAsStream("/server.keystore");
            if (is == null) {
                logger.error("SyslogInput keystore not found.");
            }
            final char[] keystorePwd = "123456".toCharArray();
            try {
                keyStore.load(is, keystorePwd);
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
                    new TrustManager[]{new TrustEveryoneTrustManager()}, null);
        } catch (Exception e) {
            logger.error("SyslogInput.getSslContext warning, e={}", e);
        }
        return sslContext;
    }

    /**
     * udp
     *
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
            logger.error("SyslogInput.udp warning, e={}", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * tcp
     *
     * @param port
     */
    private void tcp(int port) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new TCPMessageHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("SyslogInput.tcp warning, e={}", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * tls
     *
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            SSLEngine sslEngine = context.newEngine(ch.alloc());
                            ch.pipeline().addFirst("ssl", new SslHandler(sslEngine));

                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new TCPMessageHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("SyslogInput.tls warning, e={}", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void process(String ip, int port, String message) {
        Map<String, Object> event = new HashMap() {{
            put("local_ip", ip);
            put("local_port", port);
            put("message", message);
            put("@timestamp", DateUtil.getTimeNow());
        }};
        if (event != null && event.size() > 0) {
            inputQueueList.put(event);
        }
    }

    @ChannelHandler.Sharable
    class TCPMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object body) {
            InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            process(insocket.getAddress().getHostAddress(), insocket.getPort(), (String) body);
        }
    }

    class UDPMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket body) throws Exception {
            ByteBuf buf = body.copy().content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String message = new String(req, "UTF-8");
            process(body.sender().getAddress().getHostAddress(), body.sender().getPort(), message);
        }
    }

}
