package net.d4.d4lib.io.nettys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class Test {

    static final Logger log = LoggerFactory.getLogger(Test.class.getName());

    public static void main(String[] args) {
        NettyPool.getInstance().addBindTcpServer(9527);
    }
}
