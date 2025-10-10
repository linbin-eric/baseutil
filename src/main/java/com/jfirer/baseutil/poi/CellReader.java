package com.jfirer.baseutil.poi;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 将excel中的单元数据转化后写入对象
 */
public interface CellReader
{
    /**
     * 将excel中的单元数据转化后写入对象
     *
     * @param valueAccessor 这个单元数据对应的字段的访问器
     * @param instance      对象
     * @param cellValue     excel中的单元数据
     */
    void read(Cell cellValue, Object instance, ValueAccessor valueAccessor);
}
