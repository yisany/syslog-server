package org.yis.core.netty_syslog;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.entity.Message;
import org.yis.util.Utils;

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

    private Logger logger = LogManager.getLogger(TCPMessageHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        String body = (String)message;

        //信息初始化
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        InetAddress ip = insocket.getAddress();
        int port = insocket.getPort();

        Message mmsg = Utils.initMessage(ip, port, body);

        System.out.println(">>> message came: "+ JSON.toJSONString(mmsg));

        //置入内存队列
        Utils.pushToInput(mmsg);
    }
}
