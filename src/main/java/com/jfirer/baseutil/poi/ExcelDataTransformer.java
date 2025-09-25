package com.jfirer.baseutil.poi;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;

public interface ExcelDataTransformer
{
    void transform(ValueAccessor valueAccessor, Object instance, Object cellValue);
}
