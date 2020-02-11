package com.jfirer.baseutil.reflect.copy;

import java.lang.reflect.Field;

public class ReflectPropertyCopyDescriptorFactory extends AbstractPropertyCopyDescriptorFactory
{
    public static final ReflectPropertyCopyDescriptorFactory instance = new ReflectPropertyCopyDescriptorFactory();

    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateEnumCopyPropertyCopyDescriptor(Class<S> s, Class<D> d, final Field fromProperty, final Field toProperty)
    {
        return new PropertyCopyDescriptor<S, D>()
        {
            private Class<?> desEnumType = toProperty.getType();

            @Override
            public String fromProperty()
            {
                return fromProperty.getName();
            }

            @Override
            public String toProperty()
            {
                return toProperty.getName();
            }

            @Override
            public void process(S source, D des) throws Exception
            {
                Enum<?> instance = (Enum<?>) fromProperty.get(source);
                if (instance == null)
                {
                    return;
                }
                @SuppressWarnings({"rawtypes", "unchecked"}) Enum desEnumInstance = Enum.valueOf((Class<Enum>) desEnumType, instance.name());
                toProperty.set(des, desEnumInstance);
            }
        };
    }

    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateDefaultCopyPropertyDescriptor(Class<S> s, Class<D> d, final Field fromProperty, final Field toProperty)
    {
        return new PropertyCopyDescriptor<S, D>()
        {

            @Override
            public String fromProperty()
            {
                return fromProperty.getName();
            }

            @Override
            public String toProperty()
            {
                return toProperty.getName();
            }

            @Override
            public void process(S source, D des) throws IllegalArgumentException, IllegalAccessException
            {
                toProperty.set(des, fromProperty.get(source));
            }
        };
    }
}
