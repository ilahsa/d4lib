package net.d4.d4lib.timer;

import java.util.Calendar;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.d4.d4lib.scriptpool.ScriptLoader;
import net.d4.d4lib.thread.TimerTaskModel;
import net.d4.d4lib.timer.script.IMinuteEventTimerScript;
import net.d4.d4lib.timer.script.ISecondsEventTimerScript;

/**
 *
 *
 */
public class GlobTimerEvent extends TimerTaskModel {

    private static final Logger log = LoggerFactory.getLogger(GlobTimerEvent.class);
    int second = -1;
    int minute = -1;
    int hour = -1;

    /**
     * 默认一秒执行一次
     */
    public GlobTimerEvent() {
        this(1000);
    }

    public GlobTimerEvent(int intervalTime) {
        super(-1, intervalTime);
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        int sec = calendar.get(Calendar.SECOND);
        if (this.second != sec) {
            this.second = sec;
            Iterator<ISecondsEventTimerScript> secondsScripts = ScriptLoader.getInstance().getScripts(ISecondsEventTimerScript.class);
            while (secondsScripts.hasNext()) {
                ISecondsEventTimerScript next = secondsScripts.next();
                try {
                    next.run(sec);
                } catch (Exception e) {
                    log.error("执行任务错误：" + next.getClass().getName(), e);
                }
            }
        }

        int min = calendar.get(Calendar.MINUTE);
        if (this.minute != min) {
            this.minute = min;
            Iterator<IMinuteEventTimerScript> minuteScripts = ScriptLoader.getInstance().getScripts(IMinuteEventTimerScript.class);
            while (minuteScripts.hasNext()) {
                IMinuteEventTimerScript next = minuteScripts.next();
                try {
                    next.run(minute);
                } catch (Exception e) {
                    log.error("执行任务错误：" + next.getClass().getName(), e);
                }
            }
        }

        int h = calendar.get(Calendar.HOUR);
        if (this.hour != h) {
            this.hour = h;
            Iterator<IMinuteEventTimerScript> minuteScripts = ScriptLoader.getInstance().getScripts(IMinuteEventTimerScript.class);
            while (minuteScripts.hasNext()) {
                IMinuteEventTimerScript next = minuteScripts.next();
                try {
                    next.run(this.hour);
                } catch (Exception e) {
                    log.error("执行任务错误：" + next.getClass().getName(), e);
                }
            }
        }
    }

}
