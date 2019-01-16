package org.yis.netty_syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.yis.Message;
import org.yis.Utils;

import java.net.InetAddress;

/**
 * Aim: UDP连接信息处理
 * Date: 18-12-18 14:21
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class UDPMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket message) throws Exception {
        ByteBuf buf = message.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("Init cmae: " + body);
        //信息初始化
        InetAddress ip = message.sender().getAddress();
        int port = message.sender().getPort();
        Message mmsg = Utils.initMessage(ip, port, body);
        System.out.println(">>> message came: "+ mmsg.toString());

        //加入到jlogstash-input还要置入Input内存队列
        Utils.pushToInput(mmsg);
    }
}
