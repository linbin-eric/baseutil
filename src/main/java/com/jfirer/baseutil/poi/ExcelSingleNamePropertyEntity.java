package com.jfirer.baseutil.poi;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.T;

import java.util.Map;
import java.util.function.BiConsumer;

@Data
@Accessors(chain = true)
class ExcelSingleNamePropertyEntity extends ExcelPropertyEntity implements BiConsumer<Map<String, Object>, T>
{
    protected String name;

    @Override
    public void accept(Map<String, Object> row, T t)
    {
        Object value = row.get(name);
        if (value == null)
        {
            return;
        }
        setValue(t, value);
    }
}
