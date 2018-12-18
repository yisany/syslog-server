package org.yis.netty_syslog;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.yis.Message;
import org.yis.Utils;

import java.net.InetAddress;

/**
 * Aim: TCP连接信息处理
 * Date: 18-12-18 15:07
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class TCPMessageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String)msg;
//        System.out.println("message coming >> " + body);

        //信息初始化
        //TODO 这里还存在问题，ip和port无法获取
        Message message = Utils.initMessage(InetAddress.getLocalHost(), 9898, body);
        System.out.println(">>> message came: "+ message.toString());

        //加入到jlogstash-input还要置入Input内存队列
        Utils.pushToInput(message);
    }
}
