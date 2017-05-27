package com.jfireframework.baseutil.uniqueid;

import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.jfireframework.baseutil.collection.StringCache;

public class WinterId implements Uid
{
    
    class Sequencer
    {
        String                 date;
        int                    sequence;
        long                   dateDeadline;
        long                   lastTime;
        final StringCache      cache        = new StringCache();
        final DecimalFormat    numberFormat = new DecimalFormat("0000");
        final String           pattern      = "yyyyMMddHHmmssSSS_";
        final SimpleDateFormat format;
        
        public Sequencer()
        {
            fetchDateDeadline();
            format = new SimpleDateFormat(pattern);
        }
        
        void fetchDateDeadline()
        {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            dateDeadline = calendar.getTimeInMillis();
        }
        
        synchronized String next()
        {
            long time = System.currentTimeMillis();
            if (time >= dateDeadline)
            {
                sequence = 0;
                fetchDateDeadline();
            }
            if (time > lastTime)
            {
                sequence = 0;
                lastTime = time;
            }
            else
            {
                sequence += 1;
            }
            cache.clear().append(pid).append(format.format(new Date(time))).append(numberFormat.format(sequence));
            return cache.toString();
        }
        
    }
    
    private static final String      pid;
    private static volatile WinterId INSTANCE;
    private Sequencer                sequencer;
    
    private WinterId()
    {
        sequencer = new Sequencer();
    }
    
    public static final WinterId instance()
    {
        if (INSTANCE != null)
        {
            return INSTANCE;
        }
        synchronized (WinterId.class)
        {
            if (INSTANCE != null)
            {
                return INSTANCE;
            }
            INSTANCE = new WinterId();
            return INSTANCE;
        }
    }
    
    static
    {
        pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0] + "_";
    }
    
    @Override
    public byte[] generateBytes()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String generate()
    {
        return sequencer.next();
    }
    
    @Override
    public long generateLong()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String generateDigits()
    {
        return generate();
    }
    
    public static void main(String[] args) throws SocketException, UnknownHostException
    {
        WinterId id = WinterId.instance();
        for (int i = 0; i < 100; i++)
        {
            System.out.println(id.generate());
        }
    }
}
