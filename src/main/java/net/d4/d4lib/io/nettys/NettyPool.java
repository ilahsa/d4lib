package net.d4.d4lib.io.nettys;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import net.d4.d4lib.io.nettys.http.handler.IHttpPathHandler;
import org.apache.log4j.Logger;
import net.d4.d4lib.structs.ObjectGlobal;
import net.d4.d4lib.thread.ThreadManager;
import net.d4.d4lib.thread.ThreadModel;

/**
 *
 * @author Troy.Chen
 */
public class NettyPool {

    private static final Logger log = Logger.getLogger(NettyPool.class);

    private static final NettyPool instance = new NettyPool();
    private final ArrayList<NettyTcpServer> tcpServers = new ArrayList<>();
    private final ArrayList<NettyHttpServer> httpServers = new ArrayList<>();
    private final HashMap<Long, ChannelHandlerContext> sessions = new HashMap<>();

    public static final AttributeKey<Object> SessionKey = AttributeKey.valueOf("SessionKey");

    public static NettyPool getInstance() {
        return instance;
    }

    public HashMap<Long, ChannelHandlerContext> getSessions() {
        return sessions;
    }

    public void addBindHttpServer(int port, HashMap<String, IHttpPathHandler> binds) {
        NettyHttpServer httpServer = new NettyHttpServer(port, binds);
        httpServers.add(httpServer);
    }

    public void addBindTcpServer(int port) {
        NettyTcpServer nettyTcpServer = new NettyTcpServer(port);
        tcpServers.add(nettyTcpServer);
    }

    public String getIP(ChannelHandlerContext ioSession) {
        try {
            InetSocketAddress insocket = (InetSocketAddress) ioSession.channel().remoteAddress();
            return insocket.getAddress().getHostAddress();
        } catch (Exception e) {
        }
        return "";
    }

    public <T> T getSessionAttr(ChannelHandlerContext ioSession, AttributeKey<Object> key, Class<T> t) {
        Object get = ioSession.channel().attr(key).get();
        return (T) get;
    }

    public void setSessionAttr(ChannelHandlerContext ioSession, AttributeKey<Object> key, Object value) {
        ioSession.channel().attr(key).set(value);
    }

    HashMap<Integer, MessageHandler> handlerMap = new HashMap<>(0);
    MessageThread messageThread;

    public NettyPool() {
        ThreadGroup tg = new ThreadGroup("Netty消息处理器");
        messageThread = new MessageThread(tg, "Netty消息处理器");
        //ThreadManager.getInstance().addThread(messageThread);
    }

    public void sendMessage(ChannelHandlerContext session, com.google.protobuf.Message.Builder message) {
        // 获得消息ID
        Message build = message.build();
        com.google.protobuf.Descriptors.EnumValueDescriptor field = (com.google.protobuf.Descriptors.EnumValueDescriptor) build.getField(build.getDescriptorForType().findFieldByNumber(1));
        int msgID = field.getNumber();
        NettyMessageBean bean = new NettyMessageBean(msgID, build.toByteArray());
        session.writeAndFlush(bean);
    }

    public void registerMessage(NettyMessageBean messageBean) {
        messageThread.addTask(messageBean);
    }

    public void registerHandlerMessage(long threadId, int messageId, NettyTcpHandler handler, com.google.protobuf.Message.Builder message) {
        MessageHandler messageHandler = new MessageHandler(threadId, messageId, handler, message);
        handlerMap.put(messageId, messageHandler);
        log.error("注册消息：threadId：" + threadId + " messageId：" + messageId + " handler：" + handler + " message：" + message);
    }

    class MessageHandler {

        private long threadId;
        private long messageId;
        private NettyTcpHandler handler;
        private com.google.protobuf.Message.Builder message;

        public MessageHandler(long threadId, long messageId, NettyTcpHandler handler, com.google.protobuf.Message.Builder message) {
            this.threadId = threadId;
            this.messageId = messageId;
            this.handler = handler;
            this.message = message;
        }

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public NettyTcpHandler getHandler() {
            return handler;
        }

        public void setHandler(NettyTcpHandler handler) {
            this.handler = handler;
        }

        public com.google.protobuf.Message.Builder getMessage() {
            return message;
        }

        public void setMessage(com.google.protobuf.Message.Builder message) {
            this.message = message;
        }

    }

    class MessageThread extends ThreadModel {

        public MessageThread(ThreadGroup group, String name) {
            super(group, name);
        }

        /* 任务列表 */
        private final List<NettyMessageBean> taskQueue = Collections.synchronizedList(new LinkedList<NettyMessageBean>());

        /**
         * 增加新的任务 每增加一个新任务，都要唤醒任务队列
         *
         * @param newTask
         */
        public void addTask(NettyMessageBean mesg) {
            synchronized (taskQueue) {
                taskQueue.add(mesg);
                /* 唤醒队列, 开始执行 */
                taskQueue.notify();
            }
            log.debug("接受消息 消息ID <" + mesg.getMsgid() + ">");
        }

        @Override
        public void run() {
            while (ObjectGlobal.getInstance().isServerStauts()) {
                NettyMessageBean msg = null;
                while (taskQueue.isEmpty() && ObjectGlobal.getInstance().isServerStauts()) {
                    try {
                        /* 任务队列为空，则等待有新任务加入从而被唤醒 */
                        synchronized (taskQueue) {
                            taskQueue.wait(500);
                        }
                    } catch (InterruptedException ie) {
                        log.error(ie);
                    }
                }
                synchronized (taskQueue) {
                    /* 取出任务执行 */
                    if (ObjectGlobal.getInstance().isServerStauts()) {
                        msg = taskQueue.remove(0);
                    }
                }
                if (msg != null) {
                    MessageHandler get = handlerMap.get(msg.getMsgid());
                    try {
                        NettyTcpHandler newInstance = (NettyTcpHandler) get.getHandler().clone();

                        Message.Builder parseFrom = get.getMessage().clone().mergeFrom(msg.getMsgbuffer());
//                        Message parseFrom = get.getMessage()..getParserForType().parseFrom(msg.getMsgbuffer());
                        newInstance.setIoSession(msg.getChannelHandlerContext());
                        newInstance.setMessage(parseFrom.build());
                        ThreadManager.getInstance().addTask(get.getThreadId(), newInstance);
                    } catch (Exception e) {
                        log.error("工人<“" + Thread.currentThread().getName() + "”> 执行任务<" + msg.getMsgid() + "(“" + get.getMessage().getClass().getName() + "”)> 遇到错误: ", e);
                    }
                    msg = null;
                }
            }
            log.error("线程结束, 工人<“" + Thread.currentThread().getName() + "”>退出");
        }
    }
}
