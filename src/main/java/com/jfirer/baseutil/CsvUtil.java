package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.ValueAccessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

public class CsvUtil
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface CsvHeaderName
    {
        String value() default "";
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

    record CsvEntity(int index, ValueAccessor valueAccessor) {}

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
        List<T> list   = new LinkedList<>();
        String  header = reader.readLine();
        if (StringUtil.isBlank(header))
        {
            return list;
        }
        List<String> content = new ArrayList<>();
        getContent(header, content);
        int         headerCount = content.size();
        CsvEntity[] csvEntities = defineCsvHeader(type, content);
        String      line;
        while ((line = reader.readLine()) != null)
        {
            if (StringUtil.isNotBlank(line))
            {
                if (content.size() == headerCount)
                {
                    content.clear();
                }
                getContent(line, content);
                if (content.size() != headerCount)
                {
                    continue;
                }
                T t = supplier.get();
                for (CsvEntity csvEntity : csvEntities)
                {
                    try
                    {
                        csvEntity.valueAccessor.setObject(t, content.get(csvEntity.index()));
                    }
                    catch (Throwable e)
                    {
                        ReflectUtil.throwException(e);
                    }
                }
                list.add(t);
            }
        }
        return list;
    }

    private static <T> CsvEntity[] defineCsvHeader(Class<T> type, List<String> content)
    {
        List<CsvEntity>            csvEntities = new ArrayList<>();
        Map<String, ValueAccessor> map         = new HashMap<>();
        HeaderName                 headerName;
        if (type.isAnnotationPresent(CsvHeaderNameStrategy.class))
        {
            try
            {
                headerName = type.getAnnotation(CsvHeaderNameStrategy.class).value().getConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                   NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            headerName = name -> name;
        }
        Arrays.stream(type.getDeclaredFields()).forEach(field -> {
            if (field.isAnnotationPresent(CsvHeaderName.class))
            {
                map.put(field.getAnnotation(CsvHeaderName.class).value().equals("") ? field.getName() : field.getAnnotation(CsvHeaderName.class).value(), new ValueAccessor(field));
            }
            else
            {
                map.put(headerName.name(field.getName()), new ValueAccessor(field));
            }
        });
        for (int i = 0; i < content.size(); i++)
        {
            String name = content.get(i);
            if (map.containsKey(name))
            {
                ValueAccessor valueAccessor = map.get(name);
                csvEntities.add(new CsvEntity(i, valueAccessor));
            }
        }
        return csvEntities.toArray(CsvEntity[]::new);
    }

    private static void getContent(String line, List<String> list)
    {
        int end   = line.length();
        int index = 0;
        //0代表正常，1代表遇到文本
        int  state            = 0;
        int  lastContentIndex = 0;
        char c;
        while (index < end)
        {
            c = line.charAt(index);
            switch (c)
            {
                case ',' ->
                {
                    if (state == 0)
                    {
                        String content = line.substring(lastContentIndex, index);
                        if (content.length() == 0)
                        {
                            list.add("");
                        }
                        else
                        {
                            list.add(content.charAt(0) == '"' ? content.substring(1, content.length() - 1) : content);
                        }
                        lastContentIndex = index + 1;
                    }
                    else
                    {
                        ;
                    }
                }
                case '"' -> state = state == 0 ? 1 : 0;
            }
            index += 1;
        }
        String content = line.substring(lastContentIndex, index);
        if (content.length() == 0)
        {
            list.add("");
        }
        else
        {
            list.add(content.charAt(0) == '"' ? content.substring(1, content.length() - 1) : content);
        }
    }
}
