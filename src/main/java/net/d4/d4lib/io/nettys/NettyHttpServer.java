package net.d4.d4lib.io.nettys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import java.util.HashMap;
import net.d4.d4lib.io.nettys.http.handler.IHttpPathHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
class NettyHttpServer {

    private static Logger log = LoggerFactory.getLogger(NettyHttpServer.class);

    private int PORT;
    //private static MessageLite lite=AddressBookProtos.AddressBook.getDefaultInstance();
    /**
     * 用于分配处理业务线程的线程组个数
     */
    private int BIZGROUPSIZE = 2;
    /**
     * 业务出现线程大小
     */
    private int BIZTHREADSIZE = 2;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap b = new ServerBootstrap();
    private final HashMap<String, IHttpPathHandler> handlerMaps = new HashMap<>();

    public NettyHttpServer(int PORT, HashMap<String, IHttpPathHandler> handlerMap) {
        this.PORT = PORT;
        this.handlerMaps.putAll(handlerMap);
        try {
            bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
            workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new HttpRequestDecoder());
                    /**
                     * usually we receive http message infragment,if we want
                     * full http message, we should bundle HttpObjectAggregator
                     * and we can get FullHttpRequest。
                     * 我们通常接收到的是一个http片段，如果要想完整接受一次请求的所有数据，我们需要绑定HttpObjectAggregator，然后我们
                     * 就可以收到一个FullHttpRequest-是一个完整的请求信息。
                     *
                     */
                    pipeline.addLast("servercodec", new HttpServerCodec());
                    pipeline.addLast("aggegator", new HttpObjectAggregator(1024 * 1024 * 64));//定义缓冲数据量
                    pipeline.addLast(new HttpServerHandler(NettyHttpServer.this.handlerMaps));
                    pipeline.addLast("responseencoder", new HttpResponseEncoder());
                }
            });
            b.bind(PORT).sync();
            log.error("http服务器已启动 -> 0.0.0.0:" + PORT);
        } catch (Exception ex) {
            log.error("http服务器启动异常 -> 0.0.0.0:" + PORT, ex);
            System.exit(1);
        }
    }

    public void shutdown() {

        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();

    }

    public static void main(String[] args) throws Exception {
        HashMap binds = new HashMap();
        binds.put("/login/", new IHttpPathHandler() {

            @Override
            public void action(String urlPath, HttpRequestMessage requestMessage) {
                requestMessage.addBodyLine("<html>");
                requestMessage.addBodyLine("    <head>");
                requestMessage.addBodyLine("    </head>");
                requestMessage.addBodyLine("    <body>");
                requestMessage.addBodyLine("        sssssssssssssssssss");
                requestMessage.addBodyLine("    </body>");
                requestMessage.addBodyLine("</html>");
                requestMessage.respons(HttpRequestMessage.HTTPContentType.Html);
            }
        });
        binds.put("/", new IHttpPathHandler() {

            @Override
            public void action(String urlPath, HttpRequestMessage requestMessage) {
                requestMessage.addBody("OK");
                requestMessage.respons(HttpRequestMessage.HTTPContentType.Text);
            }
        });

        NettyHttpServer server = new NettyHttpServer(8080, binds);
    }
}
