package net.d4.d4lib.io.nettys;

import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.d4.d4lib.thread.TaskModel;

/**
 *
 */
public abstract class NettyTcpHandler extends TaskModel {

    private static final Logger log = LoggerFactory.getLogger(NettyTcpHandler.class);
    public static final AttributeKey<Boolean> BooleanKey = new AttributeKey<Boolean>("");
    private ChannelHandlerContext ioSession;
    private Message message;

    public NettyTcpHandler() {

    }

    public <T> T getSessionAttr(AttributeKey<Object> key, Class<T> t) {
        return NettyPool.getInstance().getSessionAttr(this.getIoSession(), key, t);
    }

    public void setSessionAttr(AttributeKey<Object> key, Object value) {
        NettyPool.getInstance().setSessionAttr(ioSession, key, value);
    }

    public ChannelHandlerContext getIoSession() {
        return ioSession;
    }

    public void setIoSession(ChannelHandlerContext ioSession) {
        this.ioSession = ioSession;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}
