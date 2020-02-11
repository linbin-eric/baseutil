package com.jfirer.baseutil.reflect.copy;

public interface PropertyCopyDescriptor<S, D>
{
    String fromProperty();

    String toProperty();

    void process(S source, D des) throws Exception;
}
