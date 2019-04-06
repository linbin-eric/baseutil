package com.jfireframework.baseutil.bytecode.support;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.DefaultAnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.SupportOverrideAttributeAnnotationMetadata;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class SupportOverrideAttributeAnnotationContextFactory extends CacheableAnnotationContextFactory
{
    @Override
    protected AnnotationContext build(Class<?> ckass, ClassLoader classLoader)
    {
        List<AnnotationMetadata> annotationsOnClass = BytecodeUtil.findAnnotationsOnClass(ckass.getName().replace('.', '/'), classLoader);
        return castToSupportOverrideContext(annotationsOnClass);
    }

    private AnnotationContext castToSupportOverrideContext(List<AnnotationMetadata> annotationsOnClass)
    {
        List<AnnotationMetadata> cast = new LinkedList<AnnotationMetadata>();
        for (AnnotationMetadata each : annotationsOnClass)
        {
            SupportOverrideAttributeAnnotationMetadata castFrom = SupportOverrideAttributeAnnotationMetadata.castFrom((DefaultAnnotationMetadata) each);
            cast.add(castFrom);
        }
        return new DefaultAnnotationContext(cast);
    }

    @Override
    protected AnnotationContext build(Method method, ClassLoader classLoader)
    {
        List<AnnotationMetadata> annotationsOnMethod = BytecodeUtil.findAnnotationsOnMethod(method, classLoader);
        return castToSupportOverrideContext(annotationsOnMethod);
    }
}
