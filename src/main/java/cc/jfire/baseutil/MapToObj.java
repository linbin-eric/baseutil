package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.ReflectUtil;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MapToObj
{
    public static T from(Map<String, Object> map, Class<T> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Field[] pojoBeanSetFields = ReflectUtil.findPojoBeanSetFields(type);
        T       instance                 = type.getConstructor().newInstance();
        for (Field each : pojoBeanSetFields)
        {
            boolean b = map.containsKey(each.getName());
        }
    }
}
