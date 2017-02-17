package net.d4.d4lib.struct;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 *
 */
public class Lock implements Serializable {

    private static final long serialVersionUID = -8826946470438620130L;
    private static final Logger log = Logger.getLogger(Lock.class);

    private volatile long lastLockTime;

    public void update() {
        // TODO 建议开启
//        long now = System.currentTimeMillis();
//        long interval = now - this.lastLockTime;
//        if (this.lastLockTime > 0 && interval < 5L) { // 5毫秒内,锁了2次,需要注意
//            log.error("Lock at: " + now + " last lock at: " + this.lastLockTime + "  interval: " + interval + " ms " + Thread.currentThread().getName());
//        }
//        this.lastLockTime = System.currentTimeMillis();
    }

    public long getLastLockTime() {
        return lastLockTime;
    }

    public void setLastLockTime(long lastLockTime) {
        this.lastLockTime = lastLockTime;
    }

}
