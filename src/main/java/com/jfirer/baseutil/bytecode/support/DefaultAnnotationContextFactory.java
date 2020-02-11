package com.jfirer.baseutil.bytecode.support;

import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class DefaultAnnotationContextFactory extends CacheableAnnotationContextFactory
{

    @Override
    protected AnnotationContext build(String resourceName, ClassLoader classLoader)
    {
        List<AnnotationMetadata> annotationMetadataList = BytecodeUtil.findAnnotationsOnClass(resourceName, classLoader);
        return new DefaultAnnotationContext(annotationMetadataList);
    }

    @Override
    protected AnnotationContext build(Method method, ClassLoader classLoader)
    {
        List<AnnotationMetadata> annotationsOnMethod = BytecodeUtil.findAnnotationsOnMethod(method, classLoader);
        return new DefaultAnnotationContext(annotationsOnMethod);
    }

    @Override
    protected AnnotationContext build(Field field, ClassLoader classLoader)
    {
        List<AnnotationMetadata> annotationMetadataList = BytecodeUtil.findAnnotationsOnField(field, classLoader);
        return new DefaultAnnotationContext(annotationMetadataList);
    }
}
