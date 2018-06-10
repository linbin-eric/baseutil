package com.jfireframework.baseutil;

import com.jfireframework.baseutil.collection.StringCache;

public class Timelog
{
    private long start;
    private long end;
    
    public void start()
    {
        start = System.currentTimeMillis();
    }
    
    public void end()
    {
        end = System.currentTimeMillis();
    }
    
    public long total()
    {
        return end - start;
    }
    
    public static void main(String[] args)
    {
        System.out.println(Integer.MAX_VALUE - 1024 * 1024 * 1024);
        StringCache cache = new StringCache();
        cache.append("12345678");
        cache.delete(3);
        System.out.println(cache.toString());
        cache.append(", \r\n");
        System.out.println(cache.isCommaMeaningfulLast());
        System.out.println(cache.delete(7).toString());
    }
}
