package net.d4.d4lib.utils;

/**
 * 原子级别操作
 * 可以使用 java.util.concurrent.atomic
 */
public class AtomUtil {

    /**
     * 递增
     *
     * @param vi
     * @return
     */
    static public long inc(long vi) {
        synchronized (AtomUtil.class) {
            if (vi < Long.MAX_VALUE) {
                return vi++;
            }
            throw new UnsupportedOperationException("已经到达 " + Long.MAX_VALUE);
        }
    }

    /**
     * 递减
     *
     * @param vi
     * @return
     */
    static public long dec(long vi) {
        synchronized (AtomUtil.class) {
            if (vi > Long.MIN_VALUE) {
                return vi--;
            }
            throw new UnsupportedOperationException("已经到达 " + Long.MIN_VALUE);
        }
    }

    /**
     * 递增
     *
     * @param vi
     * @return
     */
    static public int inc(int vi) {
        synchronized (AtomUtil.class) {
            if (vi < Integer.MAX_VALUE) {
                return vi++;
            }
            throw new UnsupportedOperationException("已经到达 " + Integer.MAX_VALUE);
        }
    }

    /**
     * 递减
     *
     * @param vi
     * @return
     */
    static public int dec(int vi) {
        synchronized (AtomUtil.class) {
            if (vi > Integer.MIN_VALUE) {
                return vi--;
            }
            throw new UnsupportedOperationException("已经到达 " + Integer.MIN_VALUE);
        }
    }

}
