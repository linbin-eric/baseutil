package com.jfireframework.baseutil.uniqueid;

import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import com.jfireframework.baseutil.collection.StringCache;

public class WinterId implements Uid
{
    
    class Sequencer
    {
        String            date;
        int               sequence;
        long              dateDeadline;
        long              lastTime;
        final StringCache cache = new StringCache();
        int               pidLength;
        Calendar          now   = Calendar.getInstance();
        SequenceFormat    sequenceFormat;
        
        class SequenceFormat
        {
            int posOfYear;
            int posOfMonth;
            int posOfDay;
            int posOfHour;
            int posOfMinute;
            int posOfSecond;
            int posOfMillSecond;
            int posOfSequence;
            
            public SequenceFormat()
            {
                posOfYear = pidLength;
                posOfMonth = posOfYear + 4;
                posOfDay = posOfMonth + 2;
                posOfHour = posOfDay + 2;
                posOfMinute = posOfHour + 2;
                posOfSecond = posOfHour + 2;
                posOfMillSecond = posOfSecond + 2;
                posOfSequence = posOfMillSecond + 4;
            }
            
            void format(StringCache cache, Calendar now, int sequence)
            {
                int year = now.get(Calendar.YEAR);
                cache.append(String.valueOf(year), posOfYear);
                int month = now.get(Calendar.MONTH) + 1;
                if (month >= 10)
                {
                    cache.append(String.valueOf(month), posOfMonth);
                }
                else
                {
                    cache.append("0", posOfMonth).append(String.valueOf(month), posOfMonth + 1);
                }
                int dayInMonth = now.get(Calendar.DAY_OF_MONTH);
                if (dayInMonth >= 10)
                {
                    cache.append(String.valueOf(dayInMonth), posOfDay);
                }
                else
                {
                    cache.append("0", posOfDay).append(String.valueOf(dayInMonth), posOfDay + 1);
                }
                int hour = now.get(Calendar.HOUR_OF_DAY);
                if (hour >= 10)
                {
                    cache.append(String.valueOf(hour), posOfHour);
                }
                else
                {
                    cache.append("0", posOfHour).append(String.valueOf(hour), posOfHour + 1);
                }
                int minute = now.get(Calendar.MINUTE);
                if (minute >= 10)
                {
                    cache.append(String.valueOf(minute), posOfMinute);
                }
                else
                {
                    cache.append("0", posOfMinute).append(String.valueOf(minute), posOfMinute + 1);
                }
                int seconds = now.get(Calendar.SECOND);
                if (seconds >= 10)
                {
                    cache.append(String.valueOf(seconds), posOfSecond);
                }
                else
                {
                    cache.append("0", posOfSecond).append(String.valueOf(seconds), posOfSecond + 1);
                }
                int millSeconds = now.get(Calendar.MILLISECOND);
                if (millSeconds >= 100)
                {
                    cache.append(String.valueOf(millSeconds), posOfMillSecond);
                }
                else if (millSeconds >= 10)
                {
                    cache.append("0", posOfMillSecond).append(String.valueOf(millSeconds), posOfMillSecond + 1);
                }
                else
                {
                    cache.append("00", posOfMillSecond).append(String.valueOf(millSeconds), posOfMillSecond + 2);
                }
                cache.count(posOfMillSecond + 3);
                cache.append('_').append(sequence);
            }
        }
        
        public Sequencer()
        {
            cache.appendCharArray(pid, 0, pid.length);
            pidLength = pid.length;
            sequenceFormat = new SequenceFormat();
        }
        
        synchronized String next()
        {
            long time = System.currentTimeMillis();
            if (time > lastTime)
            {
                sequence = 0;
                lastTime = time;
            }
            else
            {
                sequence += 1;
            }
            now.setTimeInMillis(time);
            sequenceFormat.format(cache, now, sequence);
            return cache.toString();
        }
        
    }
    
    private static final char[]      pid;
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
        pid = (ManagementFactory.getRuntimeMXBean().getName().split("@")[0] + "_").toCharArray();
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
        String[] array = new String[1000];
        for (int i = 0; i < 1000; i++)
        {
            array[i] = id.generate();
        }
        for (String each : array)
        {
            System.out.println(each);
        }
    }
}
