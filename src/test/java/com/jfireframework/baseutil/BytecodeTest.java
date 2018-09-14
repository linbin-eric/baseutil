package com.jfireframework.baseutil;

import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.bytecode.structure.AnnotationInfo;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;
import com.jfireframework.baseutil.time.Timewatch;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.jfireframework.baseutil.bytecode.util.BytecodeUtil.parseMethodParamNames;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        method = BytecodeUtil.class.getMethod("loadBytecode", ClassLoader.class, String.class);
        paramNames = parseMethodParamNames(method);
        assertEquals(2, paramNames.length);
        assertEquals("loader", paramNames[0]);
        assertEquals("name", paramNames[1]);
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

    public <E extends AnnotationInfo, T extends Annotation> void tt(Class<T>[] name1, E name2) throws NoSuchMethodException
    {

    }

    @Test
    public void test3() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Method method = BytecodeTest.class.getDeclaredMethod("tt", Class[].class, AnnotationInfo.class);
        assertNotNull(method);
        String[] paramNames = parseMethodParamNames(method);
        assertEquals(2  ,paramNames.length);
        assertEquals("name1",paramNames[0]);
        assertEquals("name2",paramNames[1]);
    }
}
