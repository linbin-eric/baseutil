package com.jfirer.baseutil.poi;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.T;

import java.util.Map;
import java.util.function.BiConsumer;

@Data
@Accessors(chain = true)
class ExcelMultiNamePropertyEntity extends ExcelPropertyEntity implements BiConsumer<Map<String, Object>, T>
{
    private String[] names;

    @Override
    public void accept(Map<String, Object> row, T t)
    {
        for (String name : names)
        {
            Object value = row.get(name);
            if (value == null)
            {
                continue;
            }
            setValue(t, value);
        }
    }
}
