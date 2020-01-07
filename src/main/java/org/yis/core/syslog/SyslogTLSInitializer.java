package org.yis.core.syslog;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * March, or die.
 *
 * @Description:
 * @Created by yisany on 2020/01/07
 */
public class SyslogTLSInitializer extends ChannelInitializer<SocketChannel> {

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
