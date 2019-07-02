package org.yis.netty_syslog;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.yis.Message;
import org.yis.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Aim: TCP连接信息处理
 * Date: 18-12-18 15:07
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
@ChannelHandler.Sharable
public class TCPMessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        String body = (String)message;

        //信息初始化
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        InetAddress ip = insocket.getAddress();
        int port = insocket.getPort();

        Message mmsg = Utils.initMessage(ip, port, body);
        System.out.println(">>> message came: "+ mmsg.toString());

        //加入到jlogstash-input还要置入Input内存队列
//        Utils.pushToInput(mmsg);
    }
}
