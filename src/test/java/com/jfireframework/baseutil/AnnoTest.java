package com.jfireframework.baseutil;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.util.List;
import javax.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.anno.AnnotationUtil;
import com.jfireframework.baseutil.anno.OverridesAttribute;

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
        @OverridesAttribute(name = "value", annotation = level1value.class)
        public String value();
        
        @OverridesAttribute(name = "array", annotation = level1value.class)
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
        List<level1value> array = annotationUtil.getAnnotations(level1value.class, innrtest.class);
        Assert.assertTrue(array.get(1).value().equals("levle2-nest") || array.get(1).value().equals("levle2"));
        Assert.assertTrue(array.get(0).value().equals("levle2-nest") || array.get(0).value().equals("levle2"));
        Assert.assertNotEquals(array.get(0).value(), array.get(1).value());
    }
    
}
