package net.d4.d4lib.io.nettys;

import com.google.protobuf.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 */
public class NettyTcpClient {

    private static final Logger log = Logger.getLogger(NettyTcpClient.class);

    private String Host = "127.0.0.1";
    private int Port = 9527;
    private Bootstrap bootstrap;
    private Channel channel;
    private INettyHandler nettyHandler;

    public NettyTcpClient(String host, int port, INettyHandler nettyHandler) {
        this.Host = host;
        this.Port = port;
        this.nettyHandler = nettyHandler;
        EventLoopGroup group = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Decoder", new NettyDecoder())
                        .addLast("Encoder", new NettyEncoder())
                        .addLast("handler", new SimpleChannelInboundHandler<NettyMessageBean>() {

                            /**
                             * 收到消息
                             *
                             * @param ctx
                             * @param msg
                             * @throws Exception
                             */
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, NettyMessageBean msg) throws Exception {
                                msg.setChannelHandlerContext(ctx);
                                NettyPool.getInstance().registerMessage(msg);
                            }

                            /**
                             * 发现异常
                             *
                             * @param ctx
                             * @param cause
                             */
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                log.error(cause.getMessage(), cause);
                            }

                            /**
                             * 断开连接后
                             *
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelUnregistered(ChannelHandlerContext ctx) {
                                String sessionAttr = NettyPool.getInstance().getSessionAttr(ctx, NettyPool.SessionKey, String.class);
                                log.error("连接断开：" + sessionAttr);
                                NettyTcpClient.this.nettyHandler.closeSession(ctx);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                String sessionAttr = NettyPool.getInstance().getSessionAttr(ctx, NettyPool.SessionKey, String.class);
                                log.error("连接闲置：" + sessionAttr);
                            }

                            /**
                             * 创建链接后，链接被激活
                             *
                             * @param ctx
                             * @throws Exception
                             */
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                String uuid = UUID.randomUUID().toString();
                                NettyPool.getInstance().setSessionAttr(ctx, NettyPool.SessionKey, uuid);
                                log.info("新建：" + uuid);
                                NettyTcpClient.this.nettyHandler.channelActive(ctx);
                            }
                        });
                    }
                });
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public String toString() {
        return " Host=" + Host + ", Port=" + Port;
    }

    public void connect() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (channel == null) {
            try {
                log.info("向 " + this.toString() + " 服务器注册");
                channel = bootstrap.connect(this.Host, this.Port).channel();
            } catch (Exception e) {
                log.error("Connect", e);
            }
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public void sendMsg(com.google.protobuf.Message.Builder message) {
        if (channel != null && channel.isActive()) {
            // 获得消息ID
            Message build = message.build();
            com.google.protobuf.Descriptors.EnumValueDescriptor field = (com.google.protobuf.Descriptors.EnumValueDescriptor) build.getField(build.getDescriptorForType().findFieldByNumber(1));
            int msgID = field.getNumber();
            NettyMessageBean bean = new NettyMessageBean(msgID, build.toByteArray());
            channel.writeAndFlush(bean);
        } else {
            log.warn("消息发送失败,连接尚未建立!");
        }
    }
}
