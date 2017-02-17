package net.d4.d4lib.io.nettys;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解码器
 */
class NettyDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyDecoder.class);

    private byte ZreoByteCount = 0;
    private ByteBuf bytes;
    private final ByteOrder endianOrder = ByteOrder.LITTLE_ENDIAN;
    private long secondTime = 0;
    private int reveCount = 0;

    public NettyDecoder() {

    }

    ByteBuf bytesAction(ByteBuf inputBuf) {
        ByteBuf bufferLen = Unpooled.buffer();
        if (bytes != null) {
            bufferLen.writeBytes(bytes);
            bytes = null;
        }
        bufferLen.writeBytes(inputBuf);
        return bufferLen;
    }

    /**
     * 留存无法读取的byte等待下一次接受的数据包
     *
     * @param bs 数据包
     * @param startI 起始位置
     * @param lenI 结束位置
     */
    void bytesAction(ByteBuf intputBuf, int startI, int lenI) {
        if (lenI - startI > 0) {
            bytes = Unpooled.buffer();
            bytes.writeBytes(intputBuf, startI, lenI);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext chc, ByteBuf inputBuf, List<Object> outputMessage) {
        if (System.currentTimeMillis() - secondTime < 1000L) {
            reveCount++;
        } else {
            secondTime = System.currentTimeMillis();
            reveCount = 0;
        }

        if (reveCount > 50) {
            logger.error("发送消息过于频繁");
            chc.disconnect();
            return;
        }
        if (inputBuf.readableBytes() > 0) {
            ZreoByteCount = 0;
            //重新组装字节数组
            ByteBuf buffercontent = bytesAction(inputBuf);
            List<NettyMessageBean> megsList = new ArrayList<>(0);
            for (;;) {
                //读取 消息长度（short）和消息ID（int） 需要 8 个字节
                if (buffercontent.readableBytes() >= 8) {
                    ///读取消息长度
                    int len = buffercontent.readInt();
                    if (buffercontent.readableBytes() >= len) {
                        int messageid = buffercontent.readInt();///读取消息ID
                        ByteBuf buf = buffercontent.readBytes(len - 4);//读取可用字节数;
                        megsList.add(new NettyMessageBean(chc, messageid, buf.array()));
                        //第二次重组
                        if (buffercontent.readableBytes() > 0) {
                            bytesAction(buffercontent, buffercontent.readerIndex(), buffercontent.readableBytes());
                            buffercontent = Unpooled.buffer();
                            buffercontent.writeBytes(bytes);
                            continue;
                        } else {
                            break;
                        }
                    }
                    ///重新设置读取进度
                    buffercontent.setIndex(buffercontent.readableBytes() - 2, inputBuf.readableBytes());
                }
                ///缓存预留的字节
                bytesAction(buffercontent, buffercontent.readerIndex(), buffercontent.readableBytes());
                break;
            }
            outputMessage.addAll(megsList);
        } else {
            ZreoByteCount++;
            if (ZreoByteCount >= 3) {
                //todo 空包处理 考虑连续三次空包，断开链接
                logger.error("decode 空包处理 连续三次空包");
                chc.close();
            }
        }
    }
}
