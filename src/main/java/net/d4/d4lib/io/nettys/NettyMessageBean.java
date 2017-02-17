package net.d4.d4lib.io.nettys;

import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Troy.Chen
 */
public class NettyMessageBean {

    private static final Logger log = Logger.getLogger(NettyMessageBean.class);

    private ChannelHandlerContext channelHandlerContext;
    private int msgid;
    private byte[] msgbuffer;

    public NettyMessageBean(int msgid, byte[] msgbuffer) {
        this.msgid = msgid;
        this.msgbuffer = msgbuffer;
    }

    public NettyMessageBean(ChannelHandlerContext channelHandlerContext, int msgid) {
        this.channelHandlerContext = channelHandlerContext;
        this.msgid = msgid;
    }

    public NettyMessageBean(ChannelHandlerContext channelHandlerContext, int msgid, byte[] msgbuffer) {
        this.channelHandlerContext = channelHandlerContext;
        this.msgid = msgid;
        this.msgbuffer = msgbuffer;
    }

    public int getMsgid() {
        return msgid;
    }

    public byte[] getMsgbuffer() {
        return msgbuffer;
    }

    public void setMsgbuffer(byte[] msgbuffer) {
        this.msgbuffer = msgbuffer;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    @Override
    public String toString() {
        return "消息ID<" + msgid + '>';
    }

}
