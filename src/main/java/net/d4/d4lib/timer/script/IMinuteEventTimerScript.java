package net.d4.d4lib.timer.script;

import net.d4.d4lib.scriptpool.IBaseScript;

/**
 *
 */
public interface IMinuteEventTimerScript extends IBaseScript {

    void run(int minute);
}
