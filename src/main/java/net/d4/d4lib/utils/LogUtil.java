package net.d4.d4lib.utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 *
 */
public class LogUtil {

    static private final Logger log = Logger.getLogger(LogUtil.class);

    static private final LogUtil instance = new LogUtil();

    static public LogUtil getInstance() {
        return instance;
    }

    private final java.util.concurrent.ConcurrentLinkedQueue<String> logsQueue = new ConcurrentLinkedQueue<>();
    private volatile String UrlString;
    private String sign = "";

    private LogUtil() {

    }

    /**
     * 创建日志标题
     *
     * @param sign 验证签名
     */
    public void setLogTitle(String sign) {
        this.sign = sign;
    }

    private String setLogInfos(String string) {
        //sign算法:game serverId sendTime 方法签名(中间用空格隔开)进行MD5加密.
        long sendTime = System.currentTimeMillis();
        //原来的发送格式
//        String md5Encode = MD5Util.md5Encode(GlobalUtil.GameID + "", GlobalUtil.PlatformId + "", GlobalUtil.ServerID + "", sendTime + "", sign);
        //现在
//        int random = RandomUtils.random(10001, 10002);
        String text = GlobalUtil.GameID + "" + GlobalUtil.PlatformId + "" + GlobalUtil.getServerID() + "" + sendTime + "" + sign;

        String md5Encode = MD5Util.md5Encode(text);

        return "{\"gameId\":" + GlobalUtil.GameID + ",\"sign\":\"" + md5Encode + "\",\"platformId\":" + GlobalUtil.PlatformId + ",\"sendTime\":" + sendTime + ",\"serverId\":" + GlobalUtil.getServerID() + ",\"infos\":[" + string + "]}";
    }

    public int getLogSize() {
        return logsQueue.size();
    }

    public void setUrlString(String UrlString) {
        this.UrlString = UrlString;
        log.error("UrlString=" + UrlString);
    }

    //<editor-fold desc="增加日志 public void addLog(String paramData)">
    /**
     *
     * @param paramData json 格式字符串，这里会把字符串做base64位处理
     */
    public void addLog(String paramData) {
//        paramData = "{\"identify\":\"" + GlobalUtil.getId() + "\"," + "\"createTime\":\"" + System.currentTimeMillis() + "\"," + paramData + "}";
//        paramData = StringUtil.getBase64(paramData);
//        log.error(paramData);
//        StringUtil..(paramData);
//        addLogQueue(paramData);
    }
    //</editor-fold>

    private long lastSendMailTime = 0;

    void addLogQueue(String paramData) {
        if (logsQueue.size() < 50000) {
            logsQueue.add(paramData);
            synchronized (logsQueue) {
                logsQueue.notify();
            }
        } else if (System.currentTimeMillis() - lastSendMailTime > 5 * 60 * 1000) {
            lastSendMailTime = System.currentTimeMillis();
            MailUtil.sendMail("日志服务器是否无法连接", "日志类型量超过：" + logsQueue.size());
        }
    }

}
