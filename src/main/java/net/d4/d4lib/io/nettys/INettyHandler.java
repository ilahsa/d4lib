package net.d4.d4lib.io.nettys;

import io.netty.channel.ChannelHandlerContext;

/**
 *
 *
 */
public interface INettyHandler {

    /**
     * 创建链接后，链接被激活
     *
     * @param session
     */
    void channelActive(ChannelHandlerContext session);

    /**
     * 断开连接
     *
     * @param session
     */
    void closeSession(ChannelHandlerContext session);
}
