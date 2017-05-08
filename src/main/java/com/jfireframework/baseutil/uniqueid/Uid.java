package com.jfireframework.baseutil.uniqueid;

public interface Uid
{
    // 该数字代表2016-01-01所具备的毫秒数，以该毫秒数作为基准
    public final long base       = 1451577660000l;
    public final int  short_mask = 0x3f;
    
    public byte[] generateBytes();
    
    public String generate();
    
    public long generateLong();
    
    public String generateDigits();
}
