package net.d4.d4lib.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @goal svnup
 * @phase compile
 * @requiresProject true
 */
public class SvnUpdate extends AbstractMojo {

    /**
     * 项目根目录
     *
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File basedir;
    /**
     * 项目资源目录
     *
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     * @readonly
     */
    private File sourceDirectory;
    /**
     * 项目测试资源目录
     *
     * @parameter expression="${project.build.testSourceDirectory}"
     * @required
     * @readonly
     */
    private File testSourceDirectory;
    /**
     * 项目资源
     *
     * @parameter expression="${project.build.resources}"
     * @required
     * @readonly
     */
    private List<Resource> resources;
    /**
     * 项目测试资源
     *
     * @parameter expression="${project.build.testResources}"
     * @required
     * @readonly
     */
    private List<Resource> testResources;

    /**
     * 指令执行结果
     */
    private final StringBuilder queryInputResult = new StringBuilder(100);

    /**
     * 输出错误的结果
     */
    private final StringBuilder queryErroInputResult = new StringBuilder(100);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            String messagepath = basedir + "\\src\\main\\messages";
            Process pro = Runtime.getRuntime().exec("svn update " + messagepath);

            CountDownLatch lock = new CountDownLatch(2);

            // 处理正常的输入流
            new CheckStreamTask(queryInputResult, lock, pro.getInputStream()).start();

            // 处理error流
            new CheckStreamTask(queryErroInputResult, lock, pro.getErrorStream()).start();

            boolean done = false;
            while (!done) {
                try {
                    lock.await();
                    done = true;
                } catch (InterruptedException e) {
                    // loop to wait
                }
            }
            String err = queryErroInputResult.toString();
            if (!err.trim().equals("")) {
                getLog().info("执行SVN UPDATE 失败!");
                getLog().info("===============================\n");
                getLog().info(err);
                getLog().info("===============================");
            } else {
                getLog().info("执行SVN UPDATE 成功!");
                getLog().info("===============================\n");
                getLog().info(queryInputResult.toString());
                getLog().info("===============================");
            }
        } catch (IOException ex) {
            getLog().error("执行SVN UPDATE 失败!", ex);
        }

    }

    /**
     * 内部类: 用来监控执行的流
     *
     * @author jiajia.lijj
     * @version $Id: RuntimeExcTransport.java, v 0.1 2012-5-1 下午03:42:21
     * Administrator Exp $
     */
    private class CheckStreamTask extends Thread {

        /**
         * 锁
         */
        private CountDownLatch lock;

        /**
         * 执行结果输入流
         */
        private InputStream inputStream;

        /**
         * 字符拼接
         */
        private StringBuilder queryInputResult;

        public CheckStreamTask(StringBuilder queryInputResult, CountDownLatch lock,
                InputStream inputStream) {
            super();
            this.lock = lock;
            this.inputStream = inputStream;
            this.queryInputResult = queryInputResult;
        }

        @Override
        public void run() {
            try {
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bf.readLine()) != null && line.length() > 0) {
                    getLog().info(line);
                    queryInputResult.append(line).append("\n");
                }
            } catch (IOException e) {
                getLog().error("读取流出错", e);
            } finally {
                lock.countDown();
            }
        }
    }
}
