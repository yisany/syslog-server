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
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");

//        String body = msg.content().toString();
//        System.out.println("message coming >> " + body);

        //信息初始化
        //TODO 这里还存在问题，ip和port无法获取
        Message message = Utils.initMessage(InetAddress.getLocalHost(), 9898, body);
        System.out.println(">>> message came: "+ message.toString());

        //加入到jlogstash-input还要置入Input内存队列
        Utils.pushToInput(message);
    }
}
