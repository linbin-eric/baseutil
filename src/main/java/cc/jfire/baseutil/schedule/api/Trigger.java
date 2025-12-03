package cc.jfire.baseutil.schedule.api;

public interface Trigger
{
    /**
     * 关联的任务
     *
     * @return
     */
    Runnable attach();

    /**
     * 该触发器是否已经取消
     *
     * @return
     */
    boolean isCanceled();

    /**
     * 类似与System.currentTimeMillis()的定义。表示下一次触发的时间.如果为负数。意味着该触发器已经结束生命周期
     *
     * @return
     */
    long deadline();

    /**
     * 计算下一次的触发时间。如果触发器还需要执行，则返回true；
     *
     * @return
     */
    boolean calNext();
}
