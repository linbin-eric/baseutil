package com.jfirer.baseutil.reflect.copy;

public interface Copy<S, D>
{
    D copy(S src, D desc);
}
