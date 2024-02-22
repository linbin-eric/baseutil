package com.jfirer.baseutil.time;

import java.util.ArrayList;

/**
 * 时间观察类
 *
 * @author 林斌（windfire@zailanghua.com）
 */
public class Timewatch
{
    private long            t0      = System.currentTimeMillis();
    private long            t1      = System.currentTimeMillis();
    private long            current = System.currentTimeMillis();
    private ArrayList<Long> records = new ArrayList<>();

    /**
     * 开始计时
     */
    public void start()
    {
        t0 = System.currentTimeMillis();
    }

    /**
     * 结束计时
     */
    public void end()
    {
        t1 = System.currentTimeMillis();
    }

    /**
     * 返回统计时间
     *
     * @return
     */
    public long getTotal()
    {
        return t1 - t0;
    }

    public void record()
    {
        long pre = current;
        current = System.currentTimeMillis();
        records.add(current - pre);
    }

    public long getRecord(int index)
    {
        return records.get(index);
    }
}
