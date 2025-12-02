package xin.nb1.baseutil.poi;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import xin.nb1.baseutil.reflect.ReflectUtil;
import xin.nb1.baseutil.reflect.valueaccessor.ValueAccessor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

@Data
public class ExcelPoiUtil
{
    private              List<Map<String, Object>>                  dataList = new ArrayList<>();
    private static final ConcurrentMap<Class<?>, ExcelEntityParser> parseMap = new ConcurrentHashMap<>();

    @SneakyThrows
    public static void writeExcel(OutputStream outputStream, List<?> dataList)
    {
        // 前置检查：如果dataList为空或null，直接返回
        if (dataList == null || dataList.isEmpty())
        {
            return;
        }
        // 获取类型信息：从第一个元素确定数据类型
        Object            firstElement      = dataList.get(0);
        Class<?>          type              = firstElement.getClass();
        ExcelEntityParser excelEntityParser = parseMap.computeIfAbsent(type, ExcelPoiUtil::parse);
        // 创建Workbook（XLS格式）
        try (HSSFWorkbook workbook = new HSSFWorkbook())
        {
            // 创建Sheet
            Sheet sheet = workbook.createSheet();
            // 写入表头（第0行）
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < excelEntityParser.getOrderedProperties().length; i++)
            {
                ExcelPropertyEntity property   = (ExcelPropertyEntity) excelEntityParser.getOrderedProperties()[i];
                Cell                headerCell = headerRow.createCell(i);
                headerCell.setCellValue(property.getNames()[0]);
            }
            // 写入数据行（从第1行开始）
            for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++)
            {
                Row    dataRow  = sheet.createRow(rowIndex + 1);
                Object instance = dataList.get(rowIndex);
                excelEntityParser.write(dataRow, instance);
            }
            // 输出到流
            workbook.write(outputStream);
        }
    }

    @SneakyThrows
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> type)
    {
        ExcelEntityParser excelEntityParser = parseMap.computeIfAbsent(type, ExcelPoiUtil::parse);
        try (Workbook workbook = createWorkbookFromStream(inputStream))
        {
            ExcelData excelData = processWorkbook(workbook, 0, (row, header) -> excelEntityParser.read(row, header));
            return (List<T>) excelData.data;
        }
    }

    @SneakyThrows
    private static ExcelEntityParser parse(Class<?> k)
    {
        Field[]                   declaredFields = k.getDeclaredFields();
        List<ExcelPropertyEntity> entities       = new LinkedList<>();
        for (Field declaredField : declaredFields)
        {
            ExcelPropertyEntity excelPropertyEntity = new ExcelPropertyEntity();
            ExcelProperty       excelProperty       = declaredField.getAnnotation(ExcelProperty.class);
            if (excelProperty == null)
            {
                excelPropertyEntity.setNames(new String[]{declaredField.getName()});
            }
            else
            {
                try
                {
                    excelPropertyEntity.setNames(excelProperty.value());
                    if (excelProperty.reader() != CellReader.class)
                    {
                        excelPropertyEntity.setCellReader(excelProperty.reader().getConstructor().newInstance());
                    }
                    if (excelProperty.writer() != CellWriter.class)
                    {
                        excelPropertyEntity.setCellWriter(excelProperty.writer().getConstructor().newInstance());
                    }
                }
                catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }
            }
            excelPropertyEntity.setValueAccessor(ValueAccessor.standard(declaredField));
            excelPropertyEntity.setClassId(ReflectUtil.getClassId(declaredField.getType()));
            entities.add(excelPropertyEntity);
        }
        Map<String, ExcelPropertyEntity> map = new HashMap<>();
        for (ExcelPropertyEntity entity : entities)
        {
            for (String name : entity.getNames())
            {
                map.put(name, entity);
            }
        }
        return new ExcelEntityParser(k.getConstructor(), map, entities.toArray(ExcelPropertyEntity[]::new));
    }

    /**
     * 通过InputStream读取Excel文件
     *
     * @param inputStream
     * @param sheetIndex,第一个sheet的下标是0
     * @return
     */
    @SneakyThrows
    public static ExcelData readExcel(InputStream inputStream, int sheetIndex)
    {
        try (Workbook workbook = createWorkbookFromStream(inputStream))
        {
            return processWorkbook(workbook, sheetIndex, (row, header) -> readDataRow(row));
        }
    }

    private static ExcelData processWorkbook(Workbook workbook, int sheetIndex, BiFunction<Row, Map<Integer, String>, Object> parser) throws IOException
    {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null)
        {
            throw new IllegalArgumentException("Sheet " + sheetIndex + " 不存在");
        }
        Iterator<Row>        rowIterator = sheet.iterator();
        boolean              isFirstRow  = true;
        Map<Integer, String> header      = null;
        List<Object>         dataList    = new ArrayList<>();
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            if (isFirstRow)
            {
                // 读取表头
                header     = readHeaders(row);
                isFirstRow = false;
            }
            else
            {
                dataList.add(parser.apply(row, header));
            }
        }
        return new ExcelData(header, dataList);
    }

    public record ExcelData(Map<Integer, String> header, List<Object> data)
    {
    }

    /**
     * 根据文件头魔数判断文件类型并创建对应的Workbook对象
     */
    private static Workbook createWorkbookFromStream(InputStream inputStream) throws IOException
    {
        // 使用 BufferedInputStream 包装，支持 mark/reset 操作
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        // 标记流位置，以便重置
        bufferedInputStream.mark(8);
        byte[] header    = new byte[8];
        int    bytesRead = bufferedInputStream.read(header);
        bufferedInputStream.reset();
        if (bytesRead < 8)
        {
            throw new IllegalArgumentException("文件太小，无法判断文件类型");
        }
        // XLSX 文件以 PK 开头 (ZIP格式)
        if (header[0] == 0x50 && header[1] == 0x4B)
        {
            return new XSSFWorkbook(bufferedInputStream);
        }
        // XLS 文件魔数 - OLE文档标识符
        else if (header[0] == (byte) 0xD0 && header[1] == (byte) 0xCF && header[2] == (byte) 0x11 && header[3] == (byte) 0xE0 && header[4] == (byte) 0xA1 && header[5] == (byte) 0xB1)
        {
            return new HSSFWorkbook(bufferedInputStream);
        }
        // 另一种可能的XLS文件头
        else if (header[0] == (byte) 0x09 && header[1] == (byte) 0x08)
        {
            return new HSSFWorkbook(bufferedInputStream);
        }
        else
        {
            throw new IllegalArgumentException("不是有效的Excel文件格式，文件头: " + Arrays.toString(header));
        }
    }

    /**
     * 读取表头信息
     *
     * @return
     */
    private static Map<Integer, String> readHeaders(Row headerRow)
    {
        Map<Integer, String> headers      = new HashMap<>();
        Iterator<Cell>       cellIterator = headerRow.cellIterator();
        while (cellIterator.hasNext())
        {
            Cell   cell        = cellIterator.next();
            int    columnIndex = cell.getColumnIndex();
            Object headerValue = getCellValueAsString(cell);
            if (headerValue instanceof String)
            {
                headers.put(columnIndex, ((String) headerValue).trim());
            }
            else
            {
                throw new IllegalArgumentException("第一行应该都是文字类型的值");
            }
        }
        return headers;
    }

    /**
     * 读取数据行
     */
    private static Map<Integer, Object> readDataRow(Row row)
    {
        Map<Integer, Object> rowData      = new HashMap<>();
        Iterator<Cell>       cellIterator = row.cellIterator();
        while (cellIterator.hasNext())
        {
            Cell   cell        = cellIterator.next();
            int    columnIndex = cell.getColumnIndex();
            Object cellValue   = getCellValueAsString(cell);
            rowData.put(columnIndex, cellValue);
        }
        return rowData;
    }

    /**
     * 返回单元格的值，值的类型有：Date、Long、Number、Boolean
     */
    public static Object getCellValueAsString(Cell cell)
    {
        if (cell == null)
        {
            return "";
        }
        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                {
                    return cell.getDateCellValue();
                }
                else
                {
                    // 避免科学计数法显示
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue)
                    {
                        return (long) numericValue;
                    }
                    else
                    {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try
                {
                    return cell.getStringCellValue();
                }
                catch (Exception e)
                {
                    try
                    {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                    catch (Exception ex)
                    {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }
}