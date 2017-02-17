package net.d4.d4lib.structs;

import java.util.Random;
import java.util.UUID;

/**
 * 辅助
 *
 */
public class ObjectGlobal {

    private static ObjectGlobal instance = new ObjectGlobal();

    public static ObjectGlobal getInstance() {
        return instance;
    }

    private final Object obj = new Object();

    private long staticID = 0;

    //服务运行状态
    private boolean serverStauts = true;

    //默认值而已
    private int serverID = new Random().nextInt(1000);

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    /**
     * 返回每秒65535个无规则ID
     *
     * @return
     */
    public long getCreateId() {
        synchronized (obj) {
            staticID += 1;
            return (serverID & 0xFFFF) << 48 | (System.currentTimeMillis() / 1000L & 0xFFFFFFFF) << 16 | staticID & 0xFFFF;
        }
    }

    /**
     * 根据uuid生成id
     *
     * @return
     */
    public long getCreateId2() {
        String toString = UUID.randomUUID().toString();
        byte[] bytes = toString.getBytes();
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (bytes[ix] & 0xff);
        }
        return num;
    }

    public boolean isServerStauts() {
        return serverStauts;
    }

    public void setServerStauts(boolean serverStauts) {
        this.serverStauts = serverStauts;
    }

}
