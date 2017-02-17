package net.d4.d4lib.thread;

import java.util.HashMap;
import net.d4.d4lib.timer.GlobTimerEvent;
import org.apache.log4j.Logger;

/**
 *
 *
 */
public class ThreadManager {

    private static final Logger log = Logger.getLogger(ThreadManager.class);
    private static final ThreadManager instance = new ThreadManager();

    public static ThreadManager getInstance() {
        return instance;
    }
    private long GlobThread = 0;
    private long GlobTimerThread = 0;
    private final ThreadGroup GlobThreadGroup = new ThreadGroup("全局线程");

    private final HashMap<Long, ThreadModel> threadMap = new HashMap<>();

    private ThreadManager() {

    }

    public void init() {
        ThreadModel threadModelGlob = new ThreadModel(GlobThreadGroup, "全局主线程");
        GlobThread = threadModelGlob.getId();
        ThreadModel threadModelGlobTimer = new ThreadModel(GlobThreadGroup, "全局定时器线程");
        GlobTimerThread = threadModelGlobTimer.getId();
        threadModelGlobTimer.addTimer(new GlobTimerEvent());

        threadMap.put(GlobThread, threadModelGlob);
        threadMap.put(GlobTimerThread, threadModelGlobTimer);
    }

    public long getGlobThread() {
        return GlobThread;
    }

    public void setGlobThread(long GlobThread) {
        this.GlobThread = GlobThread;
    }

    public long getGlobTimerThread() {
        return GlobTimerThread;
    }

    public void setGlobTimerThread(long GlobTimerThread) {
        this.GlobTimerThread = GlobTimerThread;
    }

    public ThreadModel getGlobThreadModel() {
        ThreadModel get = threadMap.get(GlobThread);
        return get;
    }

    public ThreadModel getGlobTimerThreadModel() {
        ThreadModel get = threadMap.get(GlobTimerThread);
        return get;
    }

    public ThreadGroup getGlobThreadGroup() {
        return GlobThreadGroup;
    }

    public ThreadModel getThreadModel(long threadId) {
        ThreadModel get = threadMap.get(threadId);
        if (get == null) {
            log.error("无法找到线程模型：" + threadId);
        }
        return get;
    }

    public HashMap<Long, ThreadModel> getThreadMap() {
        return threadMap;
    }

    public long addThreadModel(ThreadGroup group, String name) {
        ThreadModel threadModel = new ThreadModel(group, name);
        addThreadModel(threadModel);
        return threadModel.getId();
    }

    public void addThreadModel(ThreadModel threadModel) {
        this.threadMap.put(threadModel.getId(), threadModel);
    }

    /**
     * false 添加失败
     *
     * @param threadId
     * @param task
     * @return
     */
    public boolean addTask(long threadId, TaskModel task) {
        ThreadModel threadModel = getThreadModel(threadId);
        if (threadModel != null) {
            threadModel.addTask(task);
            return true;
        }
        return false;
    }

    public boolean addTimerTask(long threadId, TimerTaskModel task) {
        ThreadModel threadModel = getThreadModel(threadId);
        if (threadModel != null) {
            threadModel.addTimer(task);
            return true;
        }
        return false;
    }

}
