package com.jfirer.baseutil;

import com.jfirer.baseutil.bytecode.ClassFile;
import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.SupportOverrideAttributeAnnotationMetadata;
import com.jfirer.baseutil.bytecode.structure.AnnotationInfo;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.OverridesAttribute;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;
import com.jfirer.baseutil.time.Timewatch;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class BytecodeTest
{

    /**
     * 测试获取方法名称的正确性
     */
    @Test
    public void test() throws NoSuchMethodException
    {
        Method   method     = BytecodeUtil.class.getMethod("loadBytecode", ClassLoader.class, String.class);
        String[] paramNames = BytecodeUtil.parseMethodParamNames(method);
        assertEquals(2, paramNames.length);
        assertEquals("loader", paramNames[0]);
        assertEquals("name", paramNames[1]);
    }

    static class TestConstructorName
    {
        public TestConstructorName(String name, int age)
        {
        }

        public TestConstructorName(String name, float age2)
        {
        }
    }

    /**
     * 测试获取构造方法入参名称的正确性
     */
    @Test
    public void test6() throws NoSuchMethodException
    {
        Constructor<TestConstructorName> constructor = TestConstructorName.class.getDeclaredConstructor(String.class, int.class);
        String[]                         names       = BytecodeUtil.parseConstructorParamNames(constructor);
        assertEquals("name", names[0]);
        assertEquals("age", names[1]);
    }

    /**
     * 测试速度
     */
    @Test
    public void test2() throws NoSuchMethodException
    {
        Method    method    = BytecodeUtil.class.getMethod("loadBytecode", ClassLoader.class, String.class);
        Timewatch timewatch = new Timewatch();
        for (int i = 0; i < 10000; i++)
        {
            BytecodeUtil.parseMethodParamNames(method);
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
        String[] paramNames = BytecodeUtil.parseMethodParamNames(method);
        assertEquals(2, paramNames.length);
        assertEquals("name1", paramNames[0]);
        assertEquals("name2", paramNames[1]);
    }

    @Test
    @Level2("level2")
    public void test4() throws NoSuchMethodException
    {
        Method                   test4                    = BytecodeTest.class.getDeclaredMethod("test4", null);
        AnnotationContext        annotationContext        = AnnotationContext.getInstance(test4);
        AnnotationMetadata       annotationMetadata       = annotationContext.getAnnotationMetadata(Level1.class);
        assertTrue(annotationMetadata instanceof SupportOverrideAttributeAnnotationMetadata);
        assertEquals("level2", annotationMetadata.getAttribyte("value").getStringValue());
        Level1 level1 = annotationContext.getAnnotation(Level1.class);
        assertEquals("level2", level1.value());
    }

    @Test
    public void test5()
    {
        ClassFile classFile = BytecodeUtil.loadClassFile(fortest5_2.class.getName().replace('.', '/'));
        assertTrue(classFile.hasInterface(forTest5.class));
        assertTrue(classFile.isSuperClass(fortest5_1.class));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Level1
    {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Level1("level1")
    @interface Level2
    {
        @OverridesAttribute(annotation = Level1.class, name = "value") String value();
    }

    interface forTest5
    {

    }

    abstract class fortest5_1
    {

    }

    class fortest5_2 extends fortest5_1 implements forTest5
    {

    }
}
