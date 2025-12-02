package xin.nb1.baseutil.schedule.api;

public interface Timer extends Runnable
{
    void add(Trigger trigger);

    /**
     * 结束这个timer计时器。结束之后该timer不能再被使用
     */
    void stop();
}
