package net.d4.d4lib.thread;

import org.apache.log4j.Logger;
import net.d4.d4lib.structs.ObjectAttribute;

/**
 * 任务模型
 *
 *
 */
public abstract class TaskModel implements Cloneable {

    private static final Logger log = Logger.getLogger(TaskModel.class);

    //运行时数据
    private transient ObjectAttribute tempOther = new ObjectAttribute();

    public TaskModel() {
        this.tempOther.put("submitTime", System.currentTimeMillis());
    }

    public long getSubmitTime() {
        return this.tempOther.getlongValue("submitTime");
    }

    public ObjectAttribute getTempOther() {
        return tempOther;
    }

    public void setTempOther(ObjectAttribute tempOther) {
        this.tempOther = tempOther;
    }

    public abstract void run();

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

}
