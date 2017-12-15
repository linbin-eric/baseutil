package com.jfireframework.baseutil.anno;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface CompositionAnnotation
{
    Annotation getAnnotation();
    
    Set<CompositionAnnotation> getCompositionAnnotations();
    
}
