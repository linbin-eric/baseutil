package com.jfirer.baseutil;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.OverridesAttribute;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class AnnoTest
{
    @Test
    public void test()
    {
        Assert.assertTrue(innrtest.class.isAnnotationPresent(testAnno.class));
        AnnotationContext annotationContext = AnnotationContext.getInstanceOn(innrtest.class);
        Assert.assertTrue(annotationContext.isAnnotationPresent(testAnno.class));
        Assert.assertTrue(annotationContext.isAnnotationPresent(level2nest.class));
        Assert.assertTrue(annotationContext.isAnnotationPresent(level2value.class));
        List<level1value> array = annotationContext.getAnnotations(level1value.class);
        Assert.assertTrue(array.get(1).value().equals("levle2-nest") || array.get(1).value().equals("levle2"));
        Assert.assertTrue(array.get(0).value().equals("levle2-nest") || array.get(0).value().equals("levle2"));
        Assert.assertNotEquals(array.get(0).value(), array.get(1).value());
    }

    @Resource
    @Retention(RUNTIME)
    public @interface testAnno
    {}

    @Retention(RUNTIME)
    public @interface level1value
    {
        String value();

        String[] array() default {};
    }

    @level2value(value = "levle2-nest", a = "3")
    @Retention(RUNTIME)
    public @interface level2nest
    {}

    @Retention(RUNTIME)
    @level1value(value = "level1", array = {"1", "2"})
    public @interface level2value
    {
        @OverridesAttribute(name = "value", annotation = level1value.class) String value();

        @OverridesAttribute(name = "array", annotation = level1value.class) String[] a() default {};

        @Retention(RUNTIME)
        @interface list
        {
            level2value[] value();
        }
    }

    @testAnno
    @level2value(value = "levle2", a = "3")
    @level2nest
    @level2value.list(value = {@level2value(value = "levle2-list-1", a = "3"), @level2value(value = "levle2-list-2", a = "3")})
    public static class innrtest
    {}
}
