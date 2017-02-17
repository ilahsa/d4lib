package net.d4.d4lib.io.nettys;

import org.apache.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class Test {

    static final Logger log = Logger.getLogger(Test.class.getName());

    public static void main(String[] args) {
        NettyPool.getInstance().addBindTcpServer(9527);
    }
}
