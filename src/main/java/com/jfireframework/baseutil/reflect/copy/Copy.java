package com.jfireframework.baseutil.reflect.copy;

public interface Copy<S, D>
{
    D copy(S src, D desc);
}
