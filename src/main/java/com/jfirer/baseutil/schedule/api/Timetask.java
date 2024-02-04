package com.jfirer.baseutil.schedule.api;

public interface Timetask
{
    public void invoke();
    
    /**
     * 不要在这个方法里执行判断是否取消的计数类操作，因为该方法在一次任务会被调用多次。
     * 
     * @return
     */
    public boolean isCanceled();
}
