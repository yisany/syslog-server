package com.yis.syslog.reader.syslog;

import com.yis.syslog.domain.DoubleBufferQueue;
import com.yis.syslog.domain.Message;
import com.yis.syslog.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;

/**
 * UDP连接信息处理
 */
public class UDPMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket message) throws Exception {
        ByteBuf buf = message.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        //信息初始化
        InetAddress ip = message.sender().getAddress();
        int port = message.sender().getPort();
        String timestamp = DateUtil.getUTC(System.currentTimeMillis());
        // 置入队列
        Message msg = new Message(ip, port, body, timestamp);
        DoubleBufferQueue.getInstance().push(msg);
    }
}
