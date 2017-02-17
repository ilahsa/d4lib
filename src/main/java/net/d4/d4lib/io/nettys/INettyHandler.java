package net.d4.d4lib.io.nettys;

import io.netty.channel.ChannelHandlerContext;

/**
 *
 *
 * @author 失足程序员
 * @mail 492794628@qq.com
 * @phone 13882122019
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
