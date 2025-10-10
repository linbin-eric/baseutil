# 目标

为ExcelPoiUtil增加将数据写出为Excel文件的能力。

## 思路

1. 写出方法为：`com.jfirer.baseutil.poi.ExcelPoiUtil.writeExcel`，需要实现这个方法。这个方法的dataList入参保证List中的每一个元素都是相同的类，因此可以用第一个元素来确定表头字段
2. 一个具体的类对象的写出应该实现在`com.jfirer.baseutil.poi.ExcelEntityParser`类中，为这个类增加write方法，负责写出单行数据
3. 输出Excel的时候，第一行应该要输出字段的名称（有注解用注解的名称，没有的用字段名称）

## 设计决策

1. **输出格式**：写出为XLS格式（使用HSSFWorkbook）
2. **列顺序**：按照类中字段的声明顺序来输出列
3. **表头名称**：
   - 如果字段有`@ExcelProperty`注解，使用`value()`数组的第一个元素作为表头名称
   - 如果没有注解，使用字段名称作为表头
4. **数据写入**：
   - `ExcelEntityParser.write`方法负责写出单行数据
   - `ExcelPropertyEntity.write`方法负责写出单个单元格，根据classId进行类型转换
   - 如果配置了自定义的`CellWriter`，使用自定义逻辑；否则使用默认的类型转换逻辑
5. **空值处理**：如果字段值为null，留空单元格（不调用任何setCellValue方法）
6. **char/Character类型**：转换为字符串写入Excel（使用`String.valueOf(charValue)`）
7. **空数据列表**：如果dataList为空或null，直接返回不创建文件

## 实施步骤

### 步骤1：修改ExcelEntityParser类，增加字段顺序支持
- 在`ExcelEntityParser`类中增加一个字段：`List<ExcelPropertyEntity> orderedProperties`，用于保存按声明顺序排列的属性列表
- 修改构造函数，接收这个有序列表参数
- **代码现状**：当前parse方法（ExcelPoiUtil.java:48）已经创建了有序列表`List<ExcelPropertyEntity> entities = new LinkedList<>()`，该列表按字段声明顺序构建，只需将此列表传递给ExcelEntityParser构造函数即可
- **当前构造函数签名**：`ExcelEntityParser(Constructor<T> constructor, Map<String, ExcelPropertyEntity> excelPropertyEntityMap)`（第17-18行）
- **需要修改为**：`ExcelEntityParser(Constructor<T> constructor, Map<String, ExcelPropertyEntity> excelPropertyEntityMap, List<ExcelPropertyEntity> orderedProperties)`

### 步骤2：在ExcelPropertyEntity中增加write方法
- 方法签名：`public void write(Cell cell, Object instance)`
- 实现逻辑：
  - 如果配置了自定义`cellWriter`，调用`cellWriter.write(cell, instance, valueAccessor)`
  - 否则使用默认逻辑：
    - 通过`valueAccessor.get(instance)`从`instance`中读取字段值
    - **空值处理**：如果value为null，直接返回（留空单元格）
    - 根据`classId`判断字段类型，调用对应的`cell.setCellValue()`方法：
      - **整数类型**（byte/short/int/long及包装类，对应ReflectUtil.PRIMITIVE_BYTE、PRIMITIVE_SHORT、PRIMITIVE_INT、PRIMITIVE_LONG）→ `cell.setCellValue((double)longValue)`
      - **浮点类型**（float/double及包装类，对应ReflectUtil.PRIMITIVE_FLOAT、PRIMITIVE_DOUBLE）→ `cell.setCellValue(doubleValue)`
      - **布尔类型**（boolean及Boolean，对应ReflectUtil.PRIMITIVE_BOOL、CLASS_BOOL）→ `cell.setCellValue(booleanValue)`
      - **字符类型**（char及Character，对应ReflectUtil.PRIMITIVE_CHAR、CLASS_CHAR）→ `cell.setCellValue(String.valueOf(charValue))`
      - **字符串类型**（String，对应ReflectUtil.CLASS_STRING）→ `cell.setCellValue(stringValue)`
      - **BigDecimal**类型（对应ReflectUtil.CLASS_BIGDECIMAL）→ `cell.setCellValue(bigDecimal.doubleValue())`
      - **日期类型**（Date/Timestamp/java.sql.Date，对应ReflectUtil.CLASS_DATE、CLASS_TIMESTAMP、CLASS_SQL_DATE）→ `cell.setCellValue(dateValue)`
    - 创建辅助方法`writeValue`，与`setValue`方法对称，负责将Java对象写入Cell
- **参考实现**：可以参考现有的`setValue`方法（ExcelPropertyEntity.java:39-463）的switch-case结构和类型映射

### 步骤3：在ExcelEntityParser中增加write方法
- 方法签名：`public void write(Row row, T instance)`
- 实现逻辑：
  - 遍历`orderedProperties`列表（按字段声明顺序）
  - 对每个属性，创建对应的Cell（列索引为遍历的index）
  - 调用`ExcelPropertyEntity.write(cell, instance)`将数据写入单元格

### 步骤4：修改ExcelPoiUtil.parse方法
- 在解析字段时，保持字段的声明顺序（当前已满足，使用LinkedList按顺序添加）
- 将`Field[]`转换为有序的`List<ExcelPropertyEntity>`（当前已满足）
- 创建`ExcelEntityParser`时传入这个有序列表
- **具体修改位置**：ExcelPoiUtil.java:88，将`return new ExcelEntityParser(k.getConstructor(), map);`改为`return new ExcelEntityParser(k.getConstructor(), map, entities);`

### 步骤5：实现ExcelPoiUtil.writeExcel方法
方法签名：`public static void writeExcel(OutputStream outputStream, List<?> dataList)`

实现逻辑：
1. **前置检查**：如果`dataList`为空或null，直接返回（不创建文件）
2. **获取类型信息**：从`dataList.get(0)`获取第一个元素，确定数据类型
3. **获取解析器**：通过`parseMap.computeIfAbsent`获取或创建`ExcelEntityParser`
4. **创建Workbook**：创建`HSSFWorkbook`（XLS格式）
5. **创建Sheet**：调用`workbook.createSheet()`创建工作表
6. **写入表头**（第0行）：
   - 创建表头行：`sheet.createRow(0)`
   - 遍历`ExcelEntityParser`的`orderedProperties`
   - 对每个属性，创建单元格并写入列名（`ExcelPropertyEntity.getNames()[0]`）
7. **写入数据行**：
   - 从第1行开始遍历`dataList`
   - 对每个数据对象，创建对应的行
   - 调用`ExcelEntityParser.write(row, instance)`写入该行数据
8. **输出到流**：
   - 调用`workbook.write(outputStream)`
   - 关闭`workbook`

### 步骤6：测试验证
- 创建测试类，包含各种数据类型的字段
- 使用`@ExcelProperty`注解部分字段
- 调用`writeExcel`方法写出数据
- 验证生成的Excel文件格式、列顺序、表头名称和数据内容是否正确

## 技术要点

1. **类型转换**：写入时需要根据Java类型选择合适的Cell类型：
   - 数值类型 → `cell.setCellValue(double)`
   - 字符串类型 → `cell.setCellValue(String)`
   - 布尔类型 → `cell.setCellValue(boolean)`
   - 日期类型 → `cell.setCellValue(Date)`

2. **字段顺序保证**：使用`Class.getDeclaredFields()`返回的数组顺序即为声明顺序（Java规范保证）

3. **异常处理**：需要处理IO异常和反射异常

4. **资源管理**：使用try-with-resources确保Workbook正确关闭

5. **ValueAccessor使用**：
   - **读取值**：`valueAccessor.get(instance)` - 获取对象实例的字段值
   - **写入值**：`valueAccessor.set(instance, value)` - 设置原始类型的字段值
   - **引用写入**：`valueAccessor.setReference(instance, value)` - 设置引用类型的字段值
   - **区别**：原始类型使用set方法，包装类和引用类型使用setReference方法

## 参考代码位置

- ExcelPoiUtil.java:29-31 (writeExcel方法待实现)
- ExcelPoiUtil.java:45-89 (parse方法，需要修改以支持有序属性)
- ExcelEntityParser.java:21-37 (read方法，可参考实现write方法)
- ExcelPropertyEntity.java:25-36 (read方法，可参考实现write方法)
- CellWriter.java:14 (自定义写入接口)
- ExcelProperty.java:14-16 (注解定义，包含reader和writer配置)

## 补充技术细节

### ReflectUtil.ClassId常量映射
根据代码分析，需要支持的类型常量包括：
- **原始类型**：PRIMITIVE_BYTE, PRIMITIVE_SHORT, PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_FLOAT, PRIMITIVE_DOUBLE, PRIMITIVE_CHAR, PRIMITIVE_BOOL
- **包装类型**：CLASS_BYTE, CLASS_SHORT, CLASS_INT, CLASS_LONG, CLASS_FLOAT, CLASS_DOUBLE, CLASS_CHAR, CLASS_BOOL, CLASS_STRING, CLASS_BIGDECIMAL, CLASS_DATE, CLASS_TIMESTAMP, CLASS_SQL_DATE

### CellWriter接口方法签名
```java
void write(Cell cell, Object instance, ValueAccessor accessor);
```

### 表头名称获取逻辑
- 如果字段有`@ExcelProperty`注解，使用`excelPropertyEntity.getNames()[0]`作为表头名称
- 如果没有注解，使用字段名称（即`excelPropertyEntity.getNames()[0]`，因为parse方法已经设置为字段名称）

## 注意事项

1. 保持与读取功能的对称性，确保写出的Excel文件可以被readExcel方法正确读取
2. **空值处理**：如果字段值为null，留空单元格（不调用setCellValue方法）
3. **char/Character类型处理**：需要使用`String.valueOf()`转换为字符串后写入
4. **数值类型统一处理**：Excel的数值单元格使用double类型，因此整数类型（byte/short/int/long）需要转换为double
5. **空数据列表处理**：如果dataList为空或null，直接返回不创建文件
6. 考虑大数据量场景：如果dataList很大，可能需要考虑内存优化（使用SXSSFWorkbook）
7. 保持线程安全：parseMap使用ConcurrentHashMap保证并发安全

## 关键实现要点

### ExcelEntityParser构造函数修改
- **当前签名**：只有constructor和excelPropertyEntityMap两个参数
- **需要增加**：orderedProperties参数来保存字段顺序
- **影响**：需要同时修改ExcelPoiUtil.parse方法的第88行

### 表头写入实现细节
- **创建表头行**：`sheet.createRow(0)`
- **写入表头**：遍历orderedProperties，对每个属性调用`sheet.getRow(0).createCell(columnIndex).setCellValue(headerName)`
- **headerName获取**：统一使用`excelPropertyEntity.getNames()[0]`

### 数据行写入实现细节
- **行创建**：从索引1开始创建数据行（第0行是表头）
- **数据写入**：遍历orderedProperties，创建对应列的单元格，调用`excelPropertyEntity.write(cell, instance)`
- **类型转换**：write方法内部根据classId选择合适的setCellValue方法

