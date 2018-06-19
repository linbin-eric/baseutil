package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
		Field[] desFields = getAllFields(des);
		Map<String, Field> sourceFields = generateSourceFields();
		List<PropertyCopyDescriptor<S, D>> list = new ArrayList<PropertyCopyDescriptor<S, D>>();
		for (Field toField : desFields)
		{
			if (Modifier.isFinal(toField.getModifiers()) || Modifier.isStatic(toField.getModifiers()))
			{
				continue;
			}
			if (toField.isAnnotationPresent(CopyIgnore.class))
			{
				CopyIgnore copyIgnore = toField.getAnnotation(CopyIgnore.class);
				if (copyIgnore.from() == Object.class || copyIgnore.from() == sources)
				{
					continue;
				}
			}
			if (toField.isAnnotationPresent(com.jfireframework.baseutil.reflect.copy.CopyIgnore.List.class))
			{
				com.jfireframework.baseutil.reflect.copy.CopyIgnore.List copyIgnoreList = toField.getAnnotation(com.jfireframework.baseutil.reflect.copy.CopyIgnore.List.class);
				boolean ignore = false;
				for (CopyIgnore copyIgnore : copyIgnoreList.value())
				{
					if (copyIgnore.from() == Object.class || copyIgnore.from() == sources)
					{
						ignore = true;
						break;
					}
				}
				if (ignore)
				{
					continue;
				}
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
			if (sourceProperty == null)
			{
				sourceProperty = toField.getName();
			}
			Field fromField = sourceFields.get(sourceProperty);
			if (fromField == null)
			{
				continue;
			}
			PropertyCopyDescriptor<S, D> descriptor = factory.getInstance(source, des, fromField, toField);
			list.add(descriptor);
		}
		copyDescriptors = list.toArray(new PropertyCopyDescriptor[list.size()]);
	}
	
	private Map<String, Field> generateSourceFields()
	{
		Field[] fields = getAllFields(source);
		Map<String, Field> map = new HashMap<String, Field>();
		for (Field fromField : fields)
		{
			if (Modifier.isFinal(fromField.getModifiers()) || Modifier.isStatic(fromField.getModifiers()))
			{
				continue;
			}
			if (fromField.isAnnotationPresent(CopyIgnore.class))
			{
				CopyIgnore copyIgnore = fromField.getAnnotation(CopyIgnore.class);
				if (copyIgnore.to() == Object.class || copyIgnore.to() == des)
				{
					continue;
				}
			}
			if (fromField.isAnnotationPresent(com.jfireframework.baseutil.reflect.copy.CopyIgnore.List.class))
			{
				com.jfireframework.baseutil.reflect.copy.CopyIgnore.List copyIgnoreList = fromField.getAnnotation(com.jfireframework.baseutil.reflect.copy.CopyIgnore.List.class);
				boolean ignore = false;
				for (CopyIgnore copyIgnore : copyIgnoreList.value())
				{
					if (copyIgnore.to() == Object.class || copyIgnore.to() == source)
					{
						ignore = true;
						break;
					}
				}
				if (ignore)
				{
					continue;
				}
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
			ReflectUtil.throwException(e);
			return null;
		}
		return desc;
	}
	
	/**
	 * 获取该类的所有field对象，如果子类重写了父类的field，则只包含子类的field
	 * 
	 * @param entityClass
	 * @return
	 */
	Field[] getAllFields(Class<?> entityClass)
	{
		Set<Field> set = new TreeSet<Field>(new Comparator<Field>() {
			// 只需要去重，并且希望父类的field在返回数组中排在后面，所以比较全部返回1
			@Override
			public int compare(Field o1, Field o2)
			{
				if (o1.getName().equals(o2.getName()))
				{
					return 0;
				}
				else
				{
					return 1;
				}
			}
		});
		while (entityClass != Object.class && entityClass != null)
		{
			for (Field each : entityClass.getDeclaredFields())
			{
				set.add(each);
			}
			entityClass = entityClass.getSuperclass();
		}
		return set.toArray(new Field[set.size()]);
	}
	
}
