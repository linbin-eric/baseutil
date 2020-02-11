package com.jfirer.baseutil.reflect.copy;

import java.lang.reflect.Field;

public interface PropertyCopyDescriptorFactory
{
    <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, String fromProperty, String toProperty);

    <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, Field fromProperty, Field toProperty);
}
