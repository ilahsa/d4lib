package net.d4.d4lib.io.nettys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 基于 netty 4.0.21 的 netty 服务
 *
 */
class NettyTcpServer {

    private static final Logger log = LoggerFactory.getLogger(NettyTcpServer.class);
    private int port = 9527;

    //ServerBootstrap是设置服务器的辅助类
    ServerBootstrap bs = new ServerBootstrap();

    public NettyTcpServer(int port) {
        this.port = port;
        try {
            //NioEventLoopGroup是一个多线程的I/O操作事件循环池(参数是线程数量)
            //bossGroup主要用于接受所有客户端对服务端的连接
            EventLoopGroup bossGroup = new NioEventLoopGroup(10);
            //当有新的连接进来时将会被注册到workerGroup(不提供参数，会使用默认的线程数)
            EventLoopGroup workerGroup = new NioEventLoopGroup(10);
            //group方法是将上面创建的两个EventLoopGroup实例指定到ServerBootstrap实例中去
            bs.group(bossGroup, workerGroup)
                    //channel方法用来创建通道实例(NioServerSocketChannel类来实例化一个进来的连接)
                    .channel(NioServerSocketChannel.class)
                    //为新连接到服务器的handler分配一个新的channel。ChannelInitializer用来配置新生成的channel。(如需其他的处理，继续ch.pipeline().addLast(新匿名handler对象)即可)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //处理逻辑放到 NettyClientHandler 类中去
                            ch.pipeline().addLast("Decoder", new NettyDecoder())
                            .addLast("Encoder", new NettyEncoder())
                            //.addLast("ping", new IdleStateHandler(10, 10, 10, TimeUnit.SECONDS))
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
                                    log.error(cause.getMessage());
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

                                }
                            });
                        }
                    })
                    //option()方法用于设置监听套接字
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //childOption()方法用于设置和客户端连接的套接字
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // Bind and start to accept incoming connections
            bs.bind(this.port).sync();
            log.info("开启Tcp服务端口 " + this.port + " 监听");
        } catch (Exception ex) {
            log.error("开启Tcp服务端口 " + this.port + " 监听 失败", ex);
            System.exit(0);
        }
    }
}
