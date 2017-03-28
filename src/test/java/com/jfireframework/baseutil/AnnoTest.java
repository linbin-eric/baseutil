package com.jfireframework.baseutil;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.aliasanno.AliasFor;
import com.jfireframework.baseutil.aliasanno.AnnotationUtil;
import com.jfireframework.baseutil.aliasanno.ExtendsFor;

public class AnnoTest
{
    @Resource
    @Retention(RUNTIME)
    public static @interface testAnno
    {
        
    }
    
    @testAnno
    @level2value(value = "levle2", a = "3")
    public static class innrtest
    {
        
    }
    
    @Retention(RUNTIME)
    public static @interface level1value
    {
        public String value();
        
        public String[] array() default {};
    }
    
    @Retention(RUNTIME)
    @level1value(value = "level1", array = { "1", "2" })
    public static @interface level2value
    {
        @AliasFor(value = "value", annotation = level1value.class)
        public String value();
        
        @ExtendsFor(value = "array", annotation = level1value.class)
        public String[] a() default {};
    }
    
    @Test
    public void test()
    {
        Assert.assertTrue(innrtest.class.isAnnotationPresent(testAnno.class));
        Assert.assertTrue(AnnotationUtil.isPresent(testAnno.class, innrtest.class));
        Assert.assertEquals("levle2", AnnotationUtil.getAnnotation(level1value.class, innrtest.class).value());
        Assert.assertArrayEquals(new String[] { "1", "2", "3" }, AnnotationUtil.getAnnotation(level1value.class, innrtest.class).array());
    }
}
