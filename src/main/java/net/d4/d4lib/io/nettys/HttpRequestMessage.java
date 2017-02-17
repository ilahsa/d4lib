package net.d4.d4lib.io.nettys;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 */
public class HttpRequestMessage {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestMessage.class);

    public enum HTTPContentType {

        All("*/*"),
        Text("text/plain; charset=UTF-8"),
        Json("applicaton/x-json; charset=UTF-8"),
        Html("text/html; charset=UTF-8"),
        Xml("text/xml; charset=UTF-8"),
        Javascript("application/javascript; charset=UTF-8");
        String value;

        HTTPContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

    }
    //对象
    private ChannelHandlerContext session;
    //完整的请求
    private DefaultFullHttpRequest request;
    //post或者get完整参数
    private Map<String, List<String>> params;
    //直接post或者content参数
    private String content;

    private String httpBody;

    StringBuilder builder = new StringBuilder();

    public void addBody(String msg) {
        builder.append(msg);
    }

    public void addBodyLine(String msg) {
        builder.append(msg).append("\r\n");
    }

    public void respons(HTTPContentType contentType) {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(builder.toString().getBytes("utf-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            session.writeAndFlush(response);
            session.close();
        } catch (Exception ex) {
            log.error("HttpRequestMessage.respons失败", ex);
        }
    }

    /**
     * 将会返回404错误
     */
    public void close() {
        try {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("404 NOT FOUND".getBytes("utf-8")));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, HTTPContentType.Text);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            session.writeAndFlush(response);
            session.close();
            session.close();
        } catch (UnsupportedEncodingException ex) {
        }
    }

    public ChannelHandlerContext getSession() {
        return session;
    }

    public void setSession(ChannelHandlerContext session) {
        this.session = session;
    }

    public DefaultFullHttpRequest getRequest() {
        return request;
    }

    public void setRequest(DefaultFullHttpRequest request) {
        this.request = request;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
