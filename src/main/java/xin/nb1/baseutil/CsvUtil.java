package xin.nb1.baseutil;

import xin.nb1.baseutil.reflect.ReflectUtil;
import xin.nb1.baseutil.reflect.valueaccessor.ValueAccessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CsvUtil
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CsvHeaderName
    {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface CsvHeaderNameStrategy
    {
        Class<? extends HeaderName> value();
    }

    @FunctionalInterface
    public interface HeaderName
    {
        String name(String fieldName);
    }

    record CsvEntity(int index, ValueAccessor valueAccessor, int classId, Field field)
    {
    }

    public static <T> List<T> read(BufferedReader reader, Class<T> type) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Constructor<T> constructor = type.getConstructor();
        return read(reader, type, () -> {
            try
            {
                return constructor.newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> List<T> read(BufferedReader reader, Class<T> type, Supplier<T> supplier) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Function<String, String> headerName;
        if (type.isAnnotationPresent(CsvHeaderNameStrategy.class))
        {
            try
            {
                headerName = (Function<String, String>) type.getAnnotation(CsvHeaderNameStrategy.class).value().getConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            headerName = name -> name;
        }
        return read(reader, type, supplier, headerName);
    }

    public static <T> List<T> read(BufferedReader reader, Class<T> type, Supplier<T> supplier, Function<String, String> headerNameTransfer) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        List<T> list   = new LinkedList<>();
        String  header = reader.readLine();
        if (StringUtil.isBlank(header))
        {
            return list;
        }
        List<String> content = new ArrayList<>();
        getContent(header, content);
        int           headerCount = content.size();
        CsvEntity[]   csvEntities = defineCsvHeader(type, content, headerNameTransfer);
        String        line;
        StringBuilder builder     = new StringBuilder();
        int           count       = 0;
        while ((line = reader.readLine()) != null)
        {
            if (StringUtil.isNotBlank(line))
            {
                content.clear();
                builder.append(line);
                getContent(builder.toString(), content);
                if (content.size() != headerCount)
                {
                    continue;
                }
                builder.setLength(0);
                T t = supplier.get();
                for (CsvEntity csvEntity : csvEntities)
                {
                    switch (csvEntity.classId)
                    {
                        case ReflectUtil.CLASS_INT, ReflectUtil.PRIMITIVE_INT -> csvEntity.valueAccessor.setObject(t, Integer.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_BOOL, ReflectUtil.PRIMITIVE_BOOL -> csvEntity.valueAccessor.setObject(t, Boolean.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_BYTE, ReflectUtil.PRIMITIVE_BYTE -> csvEntity.valueAccessor.setObject(t, Byte.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_SHORT, ReflectUtil.PRIMITIVE_SHORT -> csvEntity.valueAccessor.setObject(t, Short.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_LONG, ReflectUtil.PRIMITIVE_LONG -> csvEntity.valueAccessor.setObject(t, Long.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_CHAR, ReflectUtil.PRIMITIVE_CHAR -> csvEntity.valueAccessor.setObject(t, content.get(csvEntity.index()).charAt(0));
                        case ReflectUtil.CLASS_FLOAT, ReflectUtil.PRIMITIVE_FLOAT -> csvEntity.valueAccessor.setObject(t, Float.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_DOUBLE, ReflectUtil.PRIMITIVE_DOUBLE -> csvEntity.valueAccessor.setObject(t, Double.valueOf(content.get(csvEntity.index())));
                        case ReflectUtil.CLASS_STRING -> csvEntity.valueAccessor.setObject(t, content.get(csvEntity.index()));
                        case ReflectUtil.CLASS_OBJECT ->
                        {
                            throw new IllegalArgumentException("csv文件映射不支持字段:" + csvEntity.field().getName() + "的类型，请使用8种基本类型或包装类或String");
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + csvEntity.classId);
                    }
                }
                list.add(t);
            }
        }
        return list;
    }

    private static <T> CsvEntity[] defineCsvHeader(Class<T> type, List<String> content, Function<String, String> headerName)
    {
        List<CsvEntity> csvEntities = new ArrayList<>();
        record Data(ValueAccessor valueAccessor, Field field)
        {
        }
        Map<String, Data> map    = new HashMap<>();
        Class             ckass  = type;
        List<Field>       fields = new ArrayList<>();
        while (ckass != Object.class)
        {
            fields.addAll(Arrays.stream(ckass.getDeclaredFields()).toList());
            ckass = ckass.getSuperclass();
        }
        fields.forEach(field -> {
            if (field.isAnnotationPresent(CsvHeaderName.class))
            {
                map.put(field.getAnnotation(CsvHeaderName.class).value().equals("") ? field.getName() : field.getAnnotation(CsvHeaderName.class).value(), new Data(ValueAccessor.standard(field), field));
            }
            else
            {
                map.put(headerName.apply(field.getName()), new Data(ValueAccessor.standard(field), field));
            }
        });
        for (int i = 0; i < content.size(); i++)
        {
            String name = content.get(i);
            if (map.containsKey(name))
            {
                Data data = map.get(name);
                csvEntities.add(new CsvEntity(i, data.valueAccessor, ReflectUtil.getClassId(data.field.getType()), data.field));
            }
        }
        return csvEntities.toArray(CsvEntity[]::new);
    }

    private static void getContent(String line, List<String> list)
    {
        /**
         *  1、如果起始位置是"，则要遇到另外一个"才算结束。并且这个"后面要么是该行的结束，要么是,否则这个"就不是结束的。
         *  2、如果起始位置不是",则遇到另外一个,或者该行结束就算完成。
         */
        int index     = 0;
        int readBegin = 0;
        int end       = line.length() - 1;
        //0 ：未启动；1：模式 1；2：模式 2
        int mode = 0;
        while (index <= end)
        {
            switch (mode)
            {
                case 0 ->
                {
                    char c = line.charAt(index);
                    if (c == '"')
                    {
                        mode      = 1;
                        readBegin = index + 1;
                        index++;
                    }
                    else if (c == ',')
                    {
                        list.add("");
                        index++;
                    }
                    else
                    {
                        mode      = 2;
                        readBegin = index;
                        index++;
                    }
                }
                case 1 ->
                {
                    if (line.charAt(index) == '"')
                    {
                        if (index < end)
                        {
                            if (line.charAt(index + 1) == ',')
                            {
                                list.add(line.substring(readBegin, index));
                                mode = 0;
                                index += 2;
                            }
                            else
                            {
                                index++;
                            }
                        }
                        else
                        {
                            list.add(line.substring(readBegin, index));
                            mode = 0;
                            index++;
                        }
                    }
                    else
                    {
                        index++;
                    }
                }
                case 2 ->
                {
                    if (line.charAt(index) == ',')
                    {
                        list.add(line.substring(readBegin, index));
                        mode = 0;
                        index++;
                    }
                    else
                    {
                        index++;
                    }
                }
            }
        }
        if (line.charAt(end) == ',')
        {
            list.add("");
        }
        else
        {
            if (mode == 2)
            {
                list.add(line.substring(readBegin, end + 1));
            }
        }
    }
}
