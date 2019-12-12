package org.yis.core.netty_syslog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yis.entity.Message;
import org.yis.export.Export;
import org.yis.util.Utils;

import java.net.InetAddress;

/**
 * Aim: UDP连接信息处理
 * Date: 18-12-18 14:21
 * Company: www.dtstack.com
 * Author milu
 * Version: v1.0.0
 */
public class UDPMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private Logger logger = LogManager.getLogger(UDPMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket message) throws Exception {
        ByteBuf buf = message.copy().content();
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        //信息初始化
        InetAddress ip = message.sender().getAddress();
        int port = message.sender().getPort();

        Message mmsg = Utils.initMessage(ip, port, body);
        // 置入Input内存队列
        Export.pushToOut(mmsg);
    }
}
