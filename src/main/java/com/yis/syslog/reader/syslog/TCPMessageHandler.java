package com.yis.syslog.reader.syslog;

import com.yis.syslog.domain.DoubleBufferQueue;
import com.yis.syslog.domain.Message;
import com.yis.syslog.util.DateUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * TCP连接信息处理
 */
@ChannelHandler.Sharable
public class TCPMessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        String body = (String)message;

        //信息初始化
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        InetAddress ip = insocket.getAddress();
        int port = insocket.getPort();
        String timestamp = DateUtil.getUTC(System.currentTimeMillis());
        // 置入队列
        Message msg = new Message(ip, port, body, timestamp);
        DoubleBufferQueue.getInstance().push(msg);
    }
}
