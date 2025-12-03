package cc.jfire.baseutil.poi;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ExcelEntityParser<T>
{
    private final Constructor<T>                   constructor;
    private final Map<String, ExcelPropertyEntity> excelPropertyEntityMap;
    private final ExcelPropertyEntity[]            orderedProperties;

    @SneakyThrows
    public T read(Row row, Map<Integer, String> header)
    {
        T              instance     = constructor.newInstance();
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext())
        {
            Cell                cell                = cellIterator.next();
            int                 columnIndex         = cell.getColumnIndex();
            String              headerName          = header.get(columnIndex);
            ExcelPropertyEntity excelPropertyEntity = excelPropertyEntityMap.get(headerName);
            if (excelPropertyEntity != null)
            {
                excelPropertyEntity.read(cell, instance);
            }
        }
        return instance;
    }

    public void write(Row row, T instance)
    {
        for (int i = 0; i < orderedProperties.length; i++)
        {
            Cell                cell     = row.createCell(i);
            ExcelPropertyEntity property = orderedProperties[i];
            property.write(cell, instance);
        }
    }
}
