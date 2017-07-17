package com.jfireframework.baseutil.reflect.copy;

public interface Copy<S, D>
{
    public D copy(S src, D desc);
}
