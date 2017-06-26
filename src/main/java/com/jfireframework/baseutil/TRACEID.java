package com.jfireframework.baseutil;

import com.jfireframework.baseutil.uniqueid.Uid;
import com.jfireframework.baseutil.uniqueid.WinterId;

public class TRACEID
{
    private static final ThreadLocal<String> TRACEID = new ThreadLocal<String>();
    private static final Uid                 uid     = WinterId.instance();
    
    public static String newTraceId()
    {
        String traceId = uid.generateDigits();
        TRACEID.set(traceId);
        return traceId;
    }
    
    public static String currentTraceId()
    {
        return TRACEID.get();
    }
    
    public static void bind(String traceId)
    {
        TRACEID.set(traceId);
    }
}
