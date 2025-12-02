package xin.nb1.baseutil.bytecode.support;

import xin.nb1.baseutil.bytecode.annotation.AnnotationMetadata;
import xin.nb1.baseutil.bytecode.util.BytecodeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Deprecated
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
