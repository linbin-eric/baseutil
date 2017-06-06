package com.jfireframework.baseutil;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.AnnoTest.level2value.list;
import com.jfireframework.baseutil.anno.AliasFor;
import com.jfireframework.baseutil.anno.AnnotationUtil;

public class AnnoTest
{
    @Resource
    @Retention(RUNTIME)
    public static @interface testAnno
    {
        
    }
    
    @testAnno
    @level2value(value = "levle2", a = "3")
    @level2nest
    @level2value.list(value = { @level2value(value = "levle2-list-1", a = "3"), @level2value(value = "levle2-list-2", a = "3") })
    public static class innrtest
    {
        
    }
    
    @Retention(RUNTIME)
    public static @interface level1value
    {
        public String value();
        
        public String[] array() default {};
    }
    
    @level2value(value = "levle2-nest", a = "3")
    @Retention(RUNTIME)
    public static @interface level2nest
    {
        
    }
    
    @Retention(RUNTIME)
    @level1value(value = "level1", array = { "1", "2" })
    public static @interface level2value
    {
        @AliasFor(value = "value", annotation = level1value.class)
        public String value();
        
        @AliasFor(value = "array", annotation = level1value.class, isExtends = true)
        public String[] a() default {};
        
        @Retention(RUNTIME)
        public static @interface list
        {
            level2value[] value();
        }
    }
    
    @Test
    public void test()
    {
        AnnotationUtil annotationUtil = new AnnotationUtil();
        Assert.assertTrue(innrtest.class.isAnnotationPresent(testAnno.class));
        Assert.assertTrue(annotationUtil.isPresent(testAnno.class, innrtest.class));
        Assert.assertTrue(annotationUtil.isPresent(level2nest.class, innrtest.class));
        Assert.assertTrue(annotationUtil.isPresent(level2value.class, innrtest.class));
        level1value[] array = annotationUtil.getAnnotations(level1value.class, innrtest.class);
        Assert.assertTrue(array[1].value().equals("levle2-nest") || array[1].value().equals("levle2"));
        Assert.assertTrue(array[0].value().equals("levle2-nest") || array[0].value().equals("levle2"));
        Assert.assertNotEquals(array[0].value(), array[1].value());
        Assert.assertArrayEquals(new String[] { "1", "2", "3" }, annotationUtil.getAnnotation(level1value.class, innrtest.class).array());
        level1value l1 = annotationUtil.getAnnotation(level1value.class, innrtest.class);
        Annotation annotation = annotationUtil.getMetaAnnotation(l1, innrtest.class.getAnnotations());
        Assert.assertEquals(level2value.class, annotation.annotationType());
        list list = innrtest.class.getAnnotation(level2value.list.class);
        level1value[] l1_2 = annotationUtil.getAnnotations(level1value.class, list.value());
        for (level1value each : l1_2)
        {
            System.out.println(each.value());
            level2value l2 = annotationUtil.getMetaAnnotation(each, list.value());
            System.out.println(l2.value());
        }
    }
    
}
