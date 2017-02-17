package net.d4.d4lib.io.nettys;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import net.d4.d4lib.io.nettys.http.handler.IHttpPathHandler;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static Logger log = Logger.getLogger(HttpServerHandler.class);
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
    private HashMap<String, IHttpPathHandler> handlerMap;

    public HttpServerHandler(HashMap<String, IHttpPathHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof DefaultHttpRequest) {
            DefaultFullHttpRequest request = null;
            Map<String, List<String>> paramsMap = new HashMap<>();
            String httpbody = null;
            request = (DefaultFullHttpRequest) msg;
            URI uri = new URI(request.getUri());
            String urlPath = "";
            urlPath = uri.getPath();
            if (urlPath.startsWith("/")) {
                urlPath = urlPath.substring(1, urlPath.length());
            }
            if (urlPath.endsWith("/")) {
                urlPath = urlPath.substring(0, urlPath.length() - 1);
            }
            if (urlPath.equals("favicon.ico") || urlPath.equals("")) {
                log.debug(urlPath);
                close(ctx);
                return;
            }
            log.debug("URI：" + urlPath);
            if (request.getMethod().equals(HttpMethod.GET)) {
                QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
                paramsMap = decoderQuery.parameters();
            } else if (request.getMethod().equals(HttpMethod.POST)) {
                try {
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request);
                    while (decoder.hasNext()) {
                        InterfaceHttpData bodyHttpData = decoder.next();
                        if (bodyHttpData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                            Attribute attribute = (Attribute) bodyHttpData;
                            List<String> get = paramsMap.get(attribute.getName());
                            if (get == null) {
                                get = new ArrayList<>();
                                paramsMap.put(attribute.getName(), get);
                            }
                            get.add(attribute.getValue());
                        }
                    }
                } catch (HttpPostRequestDecoder.EndOfDataDecoderException end) {

                } catch (Exception e) {
                    log.error("获取参数异常", e);
                }

                ByteBuf content = request.content();
                byte[] contentbuf = new byte[content.readableBytes()];
                content.readBytes(contentbuf);
                content.release();
                httpbody = new String(contentbuf, "utf-8");
                log.debug("httpbody:" + httpbody);
            }
            for (Map.Entry<String, List<String>> attr : paramsMap.entrySet()) {
                for (String attrVal : attr.getValue()) {
                    log.debug("params: " + attr.getKey() + " =   " + attrVal);
                }
            }
            HttpRequestMessage requestMessage = new HttpRequestMessage();
            requestMessage.setContent(httpbody);
            requestMessage.setSession(ctx);
            requestMessage.setRequest(request);
            requestMessage.setParams(paramsMap);
            IHttpPathHandler get = this.handlerMap.get(urlPath);
            if (get != null) {
                get.action(urlPath, requestMessage);
            } else {
                close(ctx);
            }
            return;
        } else {
            log.info("一次无效请求：：" + msg.getClass().getName());
        }
        close(ctx);
    }

    public void close(ChannelHandlerContext session) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("404 NOT FOUND".getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        session.writeAndFlush(response);
        session.close();
        session.close();
    }
}
