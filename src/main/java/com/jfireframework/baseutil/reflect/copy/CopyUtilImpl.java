package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class CopyUtilImpl<S, D> implements CopyUtil<S, D>
{
    private CopyField[] copyFields;
    
    public CopyUtilImpl(Class<S> src, Class<D> des)
    {
        List<CopyField> copyFields = new ArrayList<CopyField>();
        Map<String, Field> srcMap = generate(src);
        Map<String, Field> descMap = generate(des);
        for (Entry<String, Field> each : srcMap.entrySet())
        {
            if (descMap.containsKey(each.getKey()))
            {
                Field descField = descMap.get(each.getKey());
                if (each.getValue().getType() == descField.getType())
                {
                    copyFields.add(CopyField.build(each.getValue(), descField));
                }
            }
        }
        this.copyFields = copyFields.toArray(new CopyField[0]);
    }
    
    private Map<String, Field> generate(Class<?> type)
    {
        Map<String, Field> map = new HashMap<String, Field>();
        for (Field each : ReflectUtil.getAllFields(type))
        {
            if (Modifier.isStatic(each.getModifiers()) //
                    || Modifier.isFinal(each.getModifiers()))
            {
                continue;
            }
            map.put(each.getName(), each);
        }
        return map;
    }
    
    public CopyUtilImpl(Class<S> src, Class<D> des, Map<String, String> nameMap)
    {
        List<CopyField> copyFields = new ArrayList<CopyField>();
        Map<String, Field> srcMap = generate(src);
        Map<String, Field> descMap = generate(des);
        for (Entry<String, Field> each : srcMap.entrySet())
        {
            Field descField = null;
            if (descMap.containsKey(each.getKey()))
            {
                descField = descMap.get(each.getKey());
            }
            else if (descMap.containsKey(nameMap.get(each.getKey())))
            {
                descField = descMap.get(nameMap.get(each.getKey()));
            }
            if (descField != null && each.getValue().getType() == descField.getType())
            {
                copyFields.add(CopyField.build(each.getValue(), descField));
            }
        }
        this.copyFields = copyFields.toArray(new CopyField[0]);
    }
    
    @Override
    public D copy(S src, D desc)
    {
        if (src == null)
        {
            return desc;
        }
        for (CopyField each : copyFields)
        {
            each.copy(src, desc);
        }
        return desc;
    }
    
}
