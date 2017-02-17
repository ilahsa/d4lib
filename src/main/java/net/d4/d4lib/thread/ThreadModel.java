package net.d4.d4lib.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 线程模型
 *
 *
 */
public class ThreadModel extends Thread {

    private static final Logger log = Logger.getLogger(ThreadModel.class);
    private static int threadID = 0;
    private static final Object SYN_OBJECT = new Object();
    private TimerRun timerRun;
    private long tid;
    /**
     * 任务列表 线程安全的任务列表
     */
    protected final List<TaskModel> taskQueue = new ArrayList<TaskModel>();
    // false标识删除线程
    private boolean runing = true;

    public ThreadModel(ThreadGroup group) {
        this(group, "无名");
    }

    public ThreadModel(ThreadGroup group, String name) {
        super(group, name);
        synchronized (SYN_OBJECT) {
            threadID++;
            tid = threadID;
        }
        timerRun = new TimerRun(group, name);
        this.start();
    }

    @Override
    public long getId() {
        return this.tid;
    }

    /**
     * 增加新的任务 每增加一个新任务，都要唤醒任务队列
     *
     * @param runnable
     */
    public void addTask(TaskModel runnable) {
        synchronized (taskQueue) {
            taskQueue.add(runnable);
            /* 唤醒队列, 开始执行 */
            taskQueue.notify();
        }
    }

    /**
     *
     * @param inserid
     * @param runnable
     */
    public void addTask(int inserid, TaskModel runnable) {
        synchronized (taskQueue) {
            taskQueue.add(inserid, runnable);
            /* 唤醒队列, 开始执行 */
            taskQueue.notify();
        }
    }

    /**
     *
     * @param runnable
     */
    public void addTimer(TimerTaskModel runnable) {
        timerRun.addTimer(runnable);
    }

    public void setRuning(boolean runing) {
        this.runing = runing;
    }

    private class TimerRun extends Thread {

        protected final List<TimerTaskModel> timerQueue = new ArrayList<TimerTaskModel>();

        public TimerRun(ThreadGroup group, String name) {
            super(group, name + "_timer");
            start();
        }

        /**
         *
         * @param runnable
         */
        public void addTimer(TimerTaskModel runnable) {
            synchronized (timerQueue) {
                timerQueue.add(runnable);
                /* 唤醒队列, 开始执行 */
                timerQueue.notify();
            }
        }

        @Override
        public void run() {
            while (ThreadModel.this.runing) {
                while (ThreadModel.this.runing && timerQueue.isEmpty()) {
                    try {
                        /* 任务队列为空，则等待有新任务加入从而被唤醒 */
                        synchronized (timerQueue) {
                            timerQueue.wait(200);
                        }
                    } catch (InterruptedException ie) {
                    }
                }
                ArrayList<TimerTaskModel> taskModels;
                synchronized (timerQueue) {
                    // 队列不为空的情况下 取出队列定时器任务
                    taskModels = new ArrayList<>(timerQueue);
                }
                if (!taskModels.isEmpty()) {
                    for (TimerTaskModel timerEvent : taskModels) {
                        int execCount = timerEvent.getTempOther().getintValue("Execcount");
                        long lastTime = timerEvent.getTempOther().getlongValue("LastExecTime");
                        long nowTime = System.currentTimeMillis();
                        if (nowTime > timerEvent.getStartTime() // 是否满足开始时间
                                && (nowTime - timerEvent.getSubmitTime() > timerEvent
                                .getIntervalTime())// 提交以后是否满足了间隔时间
                                && (timerEvent.getEndTime() <= 0 || nowTime < timerEvent
                                .getEndTime()) // 判断结束时间
                                && (nowTime - lastTime >= timerEvent
                                .getIntervalTime())) // 判断上次执行到目前是否满足间隔时间
                        {
                            // 提交执行定时器最先执行
                            ThreadModel.this.addTask(0, timerEvent);
                            // 记录
                            execCount++;
                            timerEvent.getTempOther().put("Execcount", execCount);
                            timerEvent.getTempOther().put("LastExecTime", nowTime);
                        }
                        nowTime = System.currentTimeMillis();
                        // 判断删除条件
                        if ((timerEvent.getEndTime() > 0 && nowTime < timerEvent.getEndTime())
                                || (timerEvent.getActionCount() > 0 && timerEvent.getActionCount() <= execCount)) {
                            timerQueue.remove(timerEvent);
                        }
                    }
                }
                try {
                    // 定时器， 执行方式 间隔 4ms 执行一次 把需要处理的任务放到对应的处理线程
                    Thread.sleep(4);
                } catch (InterruptedException ex) {
                }
            }
            log.error("线程结束, 工人<“" + Thread.currentThread().getName() + "”>退出");
        }
    }

    @Override
    public void run() {
        while (runing) {
            TaskModel r = null;
            while (taskQueue.isEmpty() && runing) {
                try {
                    /* 任务队列为空，则等待有新任务加入从而被唤醒 */
                    synchronized (taskQueue) {
                        taskQueue.wait(500);
                    }
                } catch (InterruptedException ie) {
                    log.error(ie);
                }
            }
            synchronized (taskQueue) {
                /* 取出任务执行 */
                if (runing) {
                    r = taskQueue.remove(0);
                }
            }
            if (r != null) {
                /* 执行任务 */
                // r.setSubmitTimeL();
                long submitTime = System.currentTimeMillis();
                try {
                    r.run();
                } catch (Exception e) {
                    log.error("工人<“" + Thread.currentThread().getName() + "”> 执行任务<" + r.getClass().getName() + "> 遇到错误: ", e);
                }
                long timeL1 = System.currentTimeMillis() - submitTime;

                if (timeL1 <= 50) {

                } else if (timeL1 <= 100L) {
                    log.info("工人<“" + Thread.currentThread().getName() + "”> 完成了任务：" + r.toString() + " 执行耗时：" + timeL1);
                } else if (timeL1 <= 1000L) {
                    log.info("工人<“" + Thread.currentThread().getName() + "”> 长时间执行 完成任务：" + r.toString() + " “考虑”任务脚本逻辑 耗时：" + timeL1);
                } else if (timeL1 <= 4000L) {
                    log.info("工人<“" + Thread.currentThread().getName() + "”> 超长时间执行完成 任务：" + r.toString() + " “检查”任务脚本逻辑 耗时：" + timeL1);
                } else {
                    log.info("工人<“" + Thread.currentThread().getName() + "”> 超长时间执行完成 任务：" + r.toString() + " “考虑是否应该删除”任务脚本 耗时：" + timeL1);
                }
                r = null;
            }
        }
        log.error("线程结束, 工人<“" + Thread.currentThread().getName() + "”>退出");
    }

    @Override
    public String toString() {
        return "Thread{" + "tid=" + tid + ",Name=" + this.getName() + '}';
    }

}
