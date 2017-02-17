package net.d4.d4lib.io.nettys.http.handler;

import net.d4.d4lib.io.nettys.HttpRequestMessage;

/**
 *
 *
 */
public interface IHttpPathHandler {

    /**
     *
     * @param urlPath 请求的目录
     * @param requestMessage 内容
     */
    void action(String urlPath, HttpRequestMessage requestMessage);

}
