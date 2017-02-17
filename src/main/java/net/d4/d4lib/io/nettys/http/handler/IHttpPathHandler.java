package net.d4.d4lib.io.nettys.http.handler;

import net.d4.d4lib.io.nettys.HttpRequestMessage;

/**
 *
 *
 * @author 失足程序员
 * @mail 492794628@qq.com
 * @phone 13882122019
 */
public interface IHttpPathHandler {

    /**
     *
     * @param urlPath 请求的目录
     * @param requestMessage 内容
     */
    void action(String urlPath, HttpRequestMessage requestMessage);

}
