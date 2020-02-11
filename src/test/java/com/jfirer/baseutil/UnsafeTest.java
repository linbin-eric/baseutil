package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.UNSAFE;
import org.junit.Assert;
import org.junit.Test;

public class UnsafeTest
{
    private final long   off  = UNSAFE.getFieldOffset("fieldName", UnsafeTest.class);
    private final long   off2 = UNSAFE.getFieldOffset("fieldName");
    private       String fieldName;

    @Test
    public void test()
    {
        long offset  = UNSAFE.getFieldOffset("fieldName", UnsafeTest.class);
        long offset1 = UNSAFE.getFieldOffset("fieldName");
        Assert.assertEquals(offset, offset1);
        Assert.assertEquals(off, off2);
    }
}
