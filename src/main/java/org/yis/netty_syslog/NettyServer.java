package org.yis.netty_syslog;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * Aim: Netty版Server
 * Date: 18-12-18 15:39
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class NettyServer {

    public static final int SYSLOG_PORT = 9898;

    public static void main(String[] args) {
        int port = SYSLOG_PORT;
        String protocol = "udp";
        if (args != null && args.length > 0){
            protocol = args[0];
        }

        if ("udp".equals(protocol)){
            // 调用udp服务
            NettyServer.udp(port);
        } else if ("tcp".equals(protocol) || "tls".equals(protocol)){
            // 调用tcp服务
            NettyServer.tcp(port);
        } else {
            System.out.println("输入有误 ！！！");
        }
    }

    /**
     * udp
     * @param port
     */
    private static void udp(int port) {
        System.out.println("This is Main.udp");
        EventLoopGroup group = new NioEventLoopGroup();
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
    private static void tcp(int port){
        System.out.println("This is Main.tcp");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
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
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
