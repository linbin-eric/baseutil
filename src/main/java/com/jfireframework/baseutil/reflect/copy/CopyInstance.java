package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class CopyInstance<S, D> implements Copy<S, D>
{
	private final Class<S>							source;
	private final Class<D>							des;
	private final PropertyCopyDescriptor<S, D>[]	copyDescriptors;
	
	@SuppressWarnings("unchecked")
	public CopyInstance(Class<S> sources, Class<D> des, PropertyCopyDescriptorFactory factory)
	{
		this.source = sources;
		this.des = des;
		Field[] allDesFields = ReflectUtil.getAllFields(des);
		Map<String, Field> allOriginFields = toFieldMap(ReflectUtil.getAllFields(source));
		List<PropertyCopyDescriptor<S, D>> list = new ArrayList<PropertyCopyDescriptor<S, D>>();
		for (Field toField : allDesFields)
		{
			if (toField.isAnnotationPresent(CopyIgnore.class))
			{
				continue;
			}
			String sourceProperty = null;
			if (toField.isAnnotationPresent(com.jfireframework.baseutil.reflect.copy.CopyFrom.List.class))
			{
				com.jfireframework.baseutil.reflect.copy.CopyFrom.List copyFroms = toField.getAnnotation(com.jfireframework.baseutil.reflect.copy.CopyFrom.List.class);
				for (CopyFrom copyFrom : copyFroms.value())
				{
					if (copyFrom.from() == sources)
					{
						sourceProperty = copyFrom.name();
						break;
					}
				}
			}
			else if (toField.isAnnotationPresent(CopyFrom.class) && toField.getAnnotation(CopyFrom.class).from() == source)
			{
				sourceProperty = toField.getAnnotation(CopyFrom.class).name();
			}
			else if (sourceProperty == null)
			{
				sourceProperty = toField.getName();
			}
			Field fromField = allOriginFields.get(sourceProperty);
			if (fromField == null)
			{
				continue;
			}
			PropertyCopyDescriptor<S, D> descriptor = factory.getInstance(source, des, fromField, toField);
			list.add(descriptor);
		}
		copyDescriptors = list.toArray(new PropertyCopyDescriptor[list.size()]);
	}
	
	private Map<String, Field> toFieldMap(Field[] fields)
	{
		Map<String, Field> map = new HashMap<String, Field>();
		for (Field fromField : fields)
		{
			if (fromField.isAnnotationPresent(CopyIgnore.class))
			{
				continue;
			}
			if (fromField.isAnnotationPresent(com.jfireframework.baseutil.reflect.copy.CopyTo.List.class))
			{
				com.jfireframework.baseutil.reflect.copy.CopyTo.List copyTos = fromField.getAnnotation(com.jfireframework.baseutil.reflect.copy.CopyTo.List.class);
				for (CopyTo copyTo : copyTos.value())
				{
					if (copyTo.to() == des)
					{
						map.put(copyTo.name(), fromField);
					}
				}
			}
			if (fromField.isAnnotationPresent(CopyTo.class) && fromField.getAnnotation(CopyTo.class).to() == des)
			{
				map.put(fromField.getAnnotation(CopyTo.class).name(), fromField);
			}
			map.put(fromField.getName(), fromField);
		}
		return map;
	}
	
	@Override
	public D copy(S src, D desc)
	{
		if (src == null || desc == null)
		{
			return desc;
		}
		try
		{
			for (PropertyCopyDescriptor<S, D> copyDescriptor : copyDescriptors)
			{
				copyDescriptor.process(src, desc);
			}
		}
		catch (Exception e)
		{
			throw new JustThrowException(e);
		}
		return desc;
	}
	
}
