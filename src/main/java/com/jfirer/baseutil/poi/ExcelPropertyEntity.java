package com.jfirer.baseutil.poi;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.T;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Accessors(chain = true)
public abstract class ExcelPropertyEntity
{
    protected ValueAccessor valueAccessor;
    protected int           classId;

    protected void setValue(T t, Object value)
    {
        switch (classId)
        {
            case ReflectUtil.PRIMITIVE_BYTE ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a.byteValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d.byteValue());
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Byte.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.byteValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 byte 类型");
                }
            }
            case ReflectUtil.CLASS_BYTE ->
            {
                if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Byte.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.byteValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Byte 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_INT ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a.intValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d.intValue());
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Integer.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.intValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 int 类型");
                }
            }
            case ReflectUtil.CLASS_INT ->
            {
                if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Integer.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.intValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Integer 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_SHORT ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a.shortValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d.shortValue());
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Short.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.shortValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 short 类型");
                }
            }
            case ReflectUtil.CLASS_SHORT ->
            {
                if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Short.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.shortValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Short 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_LONG ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a);
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d.longValue());
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Long.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.longValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 long 类型");
                }
            }
            case ReflectUtil.CLASS_LONG ->
            {
                if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Long.valueOf(s));
                }
                else if (value instanceof Long l)
                {
                    valueAccessor.setReference(t, l);
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.longValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Long 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_FLOAT ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a.floatValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d.floatValue());
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Float.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.floatValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 float 类型");
                }
            }
            case ReflectUtil.CLASS_FLOAT ->
            {
                if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Float.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.floatValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Float 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_DOUBLE ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.set(t, a.doubleValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d);
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.set(t, Double.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.set(t, n.doubleValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 double 类型");
                }
            }
            case ReflectUtil.CLASS_DOUBLE ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.setReference(t, a.doubleValue());
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.setReference(t, d);
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, Double.valueOf(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, n.doubleValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Double 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_CHAR ->
            {
                if (value instanceof String s && s.length() == 1)
                {
                    valueAccessor.set(t, s.charAt(0));
                }
                else if (value instanceof Long a && a >= 0 && a <= Character.MAX_VALUE)
                {
                    valueAccessor.set(t, (char) a.intValue());
                }
                else if (value instanceof Double d && d >= 0 && d <= Character.MAX_VALUE)
                {
                    valueAccessor.set(t, (char) d.intValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 char 类型");
                }
            }
            case ReflectUtil.CLASS_CHAR ->
            {
                if (value instanceof String s && s.length() == 1)
                {
                    valueAccessor.setReference(t, s.charAt(0));
                }
                else if (value instanceof Long a && a >= 0 && a <= Character.MAX_VALUE)
                {
                    valueAccessor.setReference(t, (char) a.intValue());
                }
                else if (value instanceof Double d && d >= 0 && d <= Character.MAX_VALUE)
                {
                    valueAccessor.setReference(t, (char) d.intValue());
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Character 类型");
                }
            }
            case ReflectUtil.PRIMITIVE_BOOL ->
            {
                if (value instanceof Boolean b)
                {
                    valueAccessor.set(t, b);
                }
                else if (value instanceof String s)
                {
                    valueAccessor.set(t, Boolean.valueOf(s));
                }
                else if (value instanceof Long a)
                {
                    valueAccessor.set(t, a != 0);
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.set(t, d != 0.0);
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 boolean 类型");
                }
            }
            case ReflectUtil.CLASS_BOOL ->
            {
                if (value instanceof Boolean b)
                {
                    valueAccessor.setReference(t, b);
                }
                else if (value instanceof String s)
                {
                    valueAccessor.setReference(t, Boolean.valueOf(s));
                }
                else if (value instanceof Long a)
                {
                    valueAccessor.setReference(t, a != 0);
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.setReference(t, d != 0.0);
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 Boolean 类型");
                }
            }
            case ReflectUtil.CLASS_STRING ->
            {
                valueAccessor.setReference(t, value.toString());
            }
            case ReflectUtil.CLASS_BIGDECIMAL ->
            {
                if (value instanceof Long a)
                {
                    valueAccessor.setReference(t, BigDecimal.valueOf(a));
                }
                else if (value instanceof Double d)
                {
                    valueAccessor.setReference(t, BigDecimal.valueOf(d));
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, new BigDecimal(s));
                }
                else if (value instanceof Number n)
                {
                    valueAccessor.setReference(t, new BigDecimal(n.toString()));
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为 BigDecimal 类型");
                }
            }
            case ReflectUtil.CLASS_DATE ->
            {
                if (value instanceof Date d)
                {
                    valueAccessor.setReference(t, d);
                }
                else if (value instanceof Long a)
                {
                    valueAccessor.setReference(t, new Date(a));
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    try
                    {
                        valueAccessor.setReference(t, new Date(Long.parseLong(s)));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("无法将字符串 '" + s + "' 转换为日期类型");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为日期类型");
                }
            }
            case ReflectUtil.CLASS_TIMESTAMP ->
            {
                if (value instanceof Date d)
                {
                    valueAccessor.setReference(t, new Timestamp(d.getTime()));
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    valueAccessor.setReference(t, new Timestamp(Long.parseLong(s)));
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为日期类型");
                }
            }
            case ReflectUtil.CLASS_SQL_DATE ->
            {
                if (value instanceof Date d)
                {
                    valueAccessor.setReference(t, new java.sql.Date(d.getTime()));
                }
                else if (value instanceof Long a)
                {
                    valueAccessor.setReference(t, new java.sql.Date(a));
                }
                else if (value instanceof String s && !s.isEmpty())
                {
                    try
                    {
                        valueAccessor.setReference(t, new java.sql.Date(Long.parseLong(s)));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IllegalArgumentException("无法将字符串 '" + s + "' 转换为日期类型");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("无法将 " + value.getClass().getSimpleName() + " 转换为日期类型");
                }
            }
            default -> valueAccessor.setReference(t, value);
        }
    }
}
