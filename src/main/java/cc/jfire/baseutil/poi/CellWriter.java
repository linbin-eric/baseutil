package cc.jfire.baseutil.poi;

import org.apache.poi.ss.usermodel.Cell;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;

public interface CellWriter
{
    /**
     * 向单元格写入数据
     * @param cell 要被写入数据的单元格
     * @param instance 对象
     * @param accessor 与该单元格对应的属性访问器
     */
    void write(Cell cell, Object instance, ValueAccessor accessor);
}
