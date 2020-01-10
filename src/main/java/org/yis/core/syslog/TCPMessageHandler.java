package org.yis.core.syslog;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.domain.DoubleBufferQueue;
import org.yis.domain.Message;
import org.yis.util.DateUtil;

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
