package com.jfirer.baseutil.poi;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

@Data
public class ExcelPoiReader
{
    private              List<Map<String, Object>>                                         dataList = new ArrayList<>();
    private              Map<Integer, String>                                              headers  = new HashMap<>();
    private static final ConcurrentMap<Class<?>, List<BiConsumer<Map<String, Object>, T>>> parseMap = new ConcurrentHashMap<>();

    @SneakyThrows
    public List<T> readExcel(InputStream inputStream, Class<T> type)
    {
        List<Map<String, Object>> excel = readExcel(inputStream);
        List<BiConsumer<Map<String, Object>, T>> consumers = parseMap.computeIfAbsent(type, k -> {
            Field[]                                  declaredFields = k.getDeclaredFields();
            List<BiConsumer<Map<String, Object>, T>> biConsumers    = new LinkedList<>();
            for (Field declaredField : declaredFields)
            {
                ExcelProperty excelProperty = declaredField.getAnnotation(ExcelProperty.class);
                if (excelProperty == null)
                {
                    throw new IllegalArgumentException("字段 " + declaredField.getName() + " 未添加 @ExcelProperty 注解");
                }
                String[] value = excelProperty.value();
                if (value.length == 1)
                {
                    biConsumers.add((BiConsumer<Map<String, Object>, T>) new ExcelSingleNamePropertyEntity().setName(excelProperty.value()[0])//
                                                                                                            .setValueAccessor(ValueAccessor.standard(declaredField))//
                                                                                                            .setClassId(ReflectUtil.getClassId(declaredField.getType())));
                }
                else
                {
                    biConsumers.add((BiConsumer<Map<String, Object>, T>) new ExcelMultiNamePropertyEntity().setNames(excelProperty.value())//
                                                                                                           .setClassId(ReflectUtil.getClassId(declaredField.getType()))//
                                                                                                           .setValueAccessor(ValueAccessor.standard(declaredField))//
                    );
                }
            }
            return biConsumers;
        });
        List<T> list = new ArrayList<>();
        for (Map<String, Object> row : excel)
        {
            T t = type.getConstructor().newInstance();
            consumers.forEach(biConsumer -> biConsumer.accept(row, t));
            list.add(t);
        }
        return list;
    }

    /**
     * 通过InputStream读取Excel文件
     *
     * @param inputStream Excel文件输入流
     * @return 读取的数据列表
     */
    public List<Map<String, Object>> readExcel(InputStream inputStream)
    {
        return readExcel(inputStream, 0);
    }

    /**
     * 通过InputStream读取Excel文件
     *
     * @param inputStream
     * @param sheetIndex
     * @return 读取的数据列表, 每行数据是一个Map, 键为表头，值为数据.值的类型有：Date、Long、Number、Boolean
     */
    @SneakyThrows
    public List<Map<String, Object>> readExcel(InputStream inputStream, int sheetIndex)
    {
        dataList.clear();
        headers.clear();
        try (Workbook workbook = createWorkbookFromStream(inputStream))
        {
            return processWorkbook(workbook, sheetIndex);
        }
    }

    /**
     * 处理Workbook对象，提取数据
     */
    private List<Map<String, Object>> processWorkbook(Workbook workbook, int sheetIndex) throws IOException
    {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null)
        {
            throw new IllegalArgumentException("Sheet " + sheetIndex + " 不存在");
        }
        Iterator<Row> rowIterator = sheet.iterator();
        boolean       isFirstRow  = true;
        int           rowIndex    = 0;
        while (rowIterator.hasNext())
        {
            Row row = rowIterator.next();
            if (isFirstRow)
            {
                // 读取表头
                readHeaders(row);
                isFirstRow = false;
            }
            else
            {
                // 读取数据行
                Map<String, Object> rowData = readDataRow(row);
                if (!rowData.isEmpty())
                {
                    dataList.add(rowData);
                }
            }
            rowIndex++;
        }
        return dataList;
    }

    /**
     * 根据文件扩展名创建对应的Workbook对象
     */
    private Workbook createWorkbook(String filePath, FileInputStream fis) throws IOException
    {
        if (filePath.endsWith(".xlsx"))
        {
            return new XSSFWorkbook(fis);
        }
        else if (filePath.endsWith(".xls"))
        {
            return new HSSFWorkbook(fis);
        }
        else
        {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 .xls 和 .xlsx 文件");
        }
    }

    /**
     * 根据文件头魔数判断文件类型并创建对应的Workbook对象
     */
    private Workbook createWorkbookFromStream(InputStream inputStream) throws IOException
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
     */
    private void readHeaders(Row headerRow)
    {
        Iterator<Cell> cellIterator = headerRow.cellIterator();
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
    }

    /**
     * 读取数据行
     */
    private Map<String, Object> readDataRow(Row row)
    {
        Map<String, Object> rowData      = new HashMap<>();
        Iterator<Cell>      cellIterator = row.cellIterator();
        while (cellIterator.hasNext())
        {
            Cell   cell        = cellIterator.next();
            int    columnIndex = cell.getColumnIndex();
            Object cellValue   = getCellValueAsString(cell);
            String columnName  = headers.get(columnIndex);
            if (columnName != null)
            {
                rowData.put(columnName, cellValue);
            }
        }
        return rowData;
    }

    /**
     * 返回单元格的值，值的类型有：Date、Long、Number、Boolean
     */
    private Object getCellValueAsString(Cell cell)
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