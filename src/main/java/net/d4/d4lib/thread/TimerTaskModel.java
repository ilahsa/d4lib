package net.d4.d4lib.thread;

/**
 * 定时器执行器
 *
 */
public abstract class TimerTaskModel extends TaskModel {

    private static final long serialVersionUID = -8331296295264699207L;

    /**
     * 开始执行的时间
     */
    private long startTime;

    /**
     * 是否一开始执行一次
     */
    private boolean isStartAction;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 执行次数
     */
    private int actionCount;

    /**
     * 间隔执行时间
     */
    private int intervalTime;

    /**
     *
     * @param startTime 指定开始时间
     * @param isStartAction 是否一开始就执行一次
     * @param endTime 指定结束时间
     * @param actionCount 指定执行次数
     * @param intervalTime 指定间隔时间
     */
    public TimerTaskModel(long startTime, boolean isStartAction, long endTime, int actionCount, int intervalTime) {
        this.startTime = startTime;
        this.isStartAction = isStartAction;
        this.endTime = endTime;
        this.actionCount = actionCount;
        this.intervalTime = intervalTime;
    }

    /**
     * 指定任务的开始执行时间
     *
     * @param startTime 指定开始时间
     * @param isStartAction 是否一开始就执行一次
     * @param actionCount 指定执行次数
     * @param intervalTime 指定间隔时间
     */
    public TimerTaskModel(long startTime, boolean isStartAction, int actionCount, int intervalTime) {
        this(startTime, isStartAction, 0, actionCount, intervalTime);
    }

    /**
     * 指定结束时间已结束时间为准，执行次数不一定够
     *
     * @param isStartAction 是否一开始就执行一次
     * @param endTime 指定结束时间
     * @param actionCount 指定执行次数
     * @param intervalTime 指定间隔时间
     *
     */
    public TimerTaskModel(boolean isStartAction, long endTime, int actionCount, int intervalTime) {
        this(0, isStartAction, endTime, actionCount, intervalTime);
    }

    /**
     * 指定开始时间，和结束时间
     *
     * @param startTime 指定开始时间
     * @param endTime 指定结束时间
     * @param intervalTime 指定间隔时间
     */
    public TimerTaskModel(long startTime, long endTime, int intervalTime) {
        this(startTime, false, endTime, -1, intervalTime);
    }

    /**
     * 指定的执行次数和间隔时间
     *
     * @param actionCount 指定执行次数
     * @param intervalTime 指定间隔时间
     */
    public TimerTaskModel(int actionCount, int intervalTime) {
        this(0, false, 0, actionCount, intervalTime);
    }

    /**
     * 提交后指定的时间以后执行一次
     *
     * @param intervalTime 指定间隔时间
     */
    public TimerTaskModel(int intervalTime) {
        this(0, false, 0, -1, intervalTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isIsStartAction() {
        return isStartAction;
    }

    public void setIsStartAction(boolean isStartAction) {
        this.isStartAction = isStartAction;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getActionCount() {
        return actionCount;
    }

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

}
