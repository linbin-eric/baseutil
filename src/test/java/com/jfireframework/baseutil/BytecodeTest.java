package com.jfireframework.baseutil;

import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.time.Timewatch;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.jfireframework.baseutil.bytecode.util.BytecodeUtil.parseMethodParamNames;
import static org.junit.Assert.assertEquals;

public class BytecodeTest
{
    /**
     * 测试获取方法名称的正确性
     */
    @Test
    public void test() throws NoSuchMethodException
    {
        Method method = AnnotationUtil.class.getDeclaredMethod("isPresent", Class.class, Field.class);
        String[] paramNames = parseMethodParamNames(method);
        assertEquals(2, paramNames.length);
        assertEquals("annoType", paramNames[0]);
        assertEquals("field", paramNames[1]);
    }

    /**
     * 测试速度
     */
    @Test
    public void test2() throws NoSuchMethodException
    {
        Method method = AnnotationUtil.class.getDeclaredMethod("isPresent", Class.class, Field.class);
        Timewatch timewatch = new Timewatch();
        for (int i = 0; i < 10000; i++)
        {
            String[] paramNames = parseMethodParamNames(method);
        }
        timewatch.end();
        System.out.println(timewatch.getTotal());
    }
}
