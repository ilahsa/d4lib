package net.d4.d4lib.io.nettys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用来主动发起HTTP请求的类
 *
 * @author Ajita
 */
public class HttpClient {

    private static Logger log = LoggerFactory.getLogger(HttpClient.class);

    public enum HTTPMethod {

        POST("POST"),
        GET("GET");
        String value;

        HTTPMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    public static void main(String[] args) throws IOException {
        HttpClient requester = new HttpClient();
        String msgString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request>"
                + "<userId>xxx</userId>"
                + "<contentId>000000000000</contentId>"
                + "<consumeCode>000000000000</consumeCode>"
                + "<cpid>701010</cpid>"
                + "<hRet>0</hRet>"
                + "<status>1800</status>"
                + "<versionId>xxx</versionId>"
                + "<cpparam>xxx</cpparam>"
                + "<packageID>2</packageID>"
                + "</request>";
        HashMap<String, String> parms = new HashMap<>();
        parms.put("keydata", msgString);
        HashMap<String, String> properties = new HashMap<>();
        properties.put("ContentType", "text/xml");
        HttpRespons send = requester.send("http://127.0.0.1:8080/ss", HTTPMethod.POST, parms, properties);
        System.out.println(send.getContent());
    }
    /**
     * 请求的默认字符编码方式
     */
    private String defaultContentEncoding;

    public String getDefaultContentEncoding() {
        return defaultContentEncoding;
    }

    public void setDefaultContentEncoding(String defaultContentEncoding) {
        this.defaultContentEncoding = defaultContentEncoding;
    }

    public HttpClient() {
        this.defaultContentEncoding = Charset.defaultCharset().name();
    }

    public HttpRespons send(String urlString, HTTPMethod method, String msg, Map<String, String> properties) throws MalformedURLException, IOException {
        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();

        if (properties != null) {
            for (String key : properties.keySet()) {
                urlConnection.addRequestProperty(key, properties.get(key));
            }
        }

        urlConnection.setRequestMethod(method.getValue());
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);

        urlConnection.getOutputStream().write(msg.getBytes());
        urlConnection.getOutputStream().flush();
        urlConnection.getOutputStream().close();

        return this.makeContent(urlString, urlConnection);
    }

    /**
     * 发送HTTP请求
     *
     * @param urlString HTTP请求的地址
     * @param method HTTP请求的方式，支持GET和POST
     * @param parameters HTTP请求内容的参数，放在GET的URL串中或者POST的请求流中
     * @param properties HTTP连接的属性
     * @return 返回一个HTTP响应对象
     * @throws IOException
     */
    public HttpRespons send(String urlString, HTTPMethod method,
            Map<String, String> parameters, Map<String, String> properties)
            throws IOException {
        HttpURLConnection urlConnection = null;

        if (method == HTTPMethod.GET && parameters != null) {
            StringBuffer param = new StringBuffer();
            int i = 0;
            for (String key : parameters.keySet()) {
                if (i == 0) {
                    param.append("?");
                } else {
                    param.append("&");
                }
                param.append(key).append("=").append(parameters.get(key));
                i++;
            }
            urlString += param;
        }
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod(method.getValue());
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);

        if (properties != null) {
            for (String key : properties.keySet()) {
                urlConnection.addRequestProperty(key, properties.get(key));
            }
        }

        if (method == HTTPMethod.POST && parameters != null) {
            StringBuilder param = new StringBuilder();
            int i = 0;
            for (String key : parameters.keySet()) {
                if (i > 0) {
                    param.append("&");
                }
                param.append(key).append("=").append(parameters.get(key));
                i++;
            }
            urlConnection.getOutputStream().write(param.toString().getBytes());
            urlConnection.getOutputStream().flush();
            urlConnection.getOutputStream().close();
        }

        return this.makeContent(urlString, urlConnection);
    }

    /**
     * 得到响应对象
     *
     * @param urlConnection
     * @return 响应对象
     * @throws IOException
     */
    private HttpRespons makeContent(String urlString, HttpURLConnection urlConnection) {
        HttpRespons httpResponser = new HttpRespons();
        try {
            String ecod = urlConnection.getContentEncoding();
            if (ecod == null) {
                ecod = this.defaultContentEncoding;
            }
            httpResponser.urlString = urlString;
            httpResponser.defaultPort = urlConnection.getURL().getDefaultPort();
            httpResponser.file = urlConnection.getURL().getFile();
            httpResponser.host = urlConnection.getURL().getHost();
            httpResponser.path = urlConnection.getURL().getPath();
            httpResponser.port = urlConnection.getURL().getPort();
            httpResponser.protocol = urlConnection.getURL().getProtocol();
            httpResponser.query = urlConnection.getURL().getQuery();
            httpResponser.ref = urlConnection.getURL().getRef();
            httpResponser.userInfo = urlConnection.getURL().getUserInfo();
            httpResponser.contentEncoding = ecod;
            httpResponser.code = urlConnection.getResponseCode();
            httpResponser.message = urlConnection.getResponseMessage();
            httpResponser.contentType = urlConnection.getContentType();
            httpResponser.method = urlConnection.getRequestMethod();
            httpResponser.connectTimeout = urlConnection.getConnectTimeout();
            httpResponser.readTimeout = urlConnection.getReadTimeout();
            if (urlConnection.getResponseCode() == 200) {
                log.error("状态码：" + urlConnection.getResponseCode());
                int len = urlConnection.getContentLength();
                InputStream in = urlConnection.getInputStream();
                byte[] buf = new byte[len];
                String line = null;
                if (in.read(buf) == len) {
                    line = new String(buf, "utf-8");
                }
                httpResponser.content = line;
            } else if (urlConnection.getResponseCode() == 404) {
                httpResponser.content = "404 NOT FOUND";
            }
        } catch (Exception e) {
            log.error("回调错误", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return httpResponser;
    }
}
