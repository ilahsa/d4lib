package net.d4.d4lib.io.nettys;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Administrator
 */
public class TestClient {

    static final Logger log = LoggerFactory.getLogger(TestClient.class);
    static NettyTcpClient client = null;

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {

            client = new NettyTcpClient("127.0.0.1", 9527, new INettyHandler() {

                @Override
                public void channelActive(ChannelHandlerContext session) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void closeSession(ChannelHandlerContext session) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            client.connect();

        }
//        BufferedReader strin = new BufferedReader(new InputStreamReader(System.in));
//        while (true) {
//            try {
//                String str = strin.readLine();
//                //构建聊天消息
//                TestMessage.ReqChatMessage.Builder chatmessage = TestMessage.ReqChatMessage.newBuilder();
//                chatmessage.setMsg(str);
//                TestClient.client.sendMsg(new NettyMessageBean(TestMessage.Proto_Login.ReqChat_VALUE, chatmessage.build().toByteArray()));
//            } catch (IOException ex) {
//            }
//        }
    }

}
