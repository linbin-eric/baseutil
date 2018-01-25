package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public class ReflectPropertyCopyDescriptorFactory implements PropertyCopyDescriptorFactory
{
	public static final ReflectPropertyCopyDescriptorFactory instance = new ReflectPropertyCopyDescriptorFactory();
	
	@Override
	public <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, String fromProperty, String toProperty)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, final Field fromProperty, final Field toProperty)
	{
		boolean hasTransfer = false;
		if (fromProperty.getType().isPrimitive())
		{
			if (fromProperty.getType() == int.class && toProperty.getType() == Integer.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == boolean.class && toProperty.getType() == Boolean.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == char.class && toProperty.getType() == Character.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == byte.class && toProperty.getType() == Byte.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == short.class && toProperty.getType() == Short.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == long.class && toProperty.getType() == Long.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == float.class && toProperty.getType() == Float.class)
			{
				hasTransfer = true;
			}
			else if (fromProperty.getType() == Double.class && toProperty.getType() == Double.class)
			{
				hasTransfer = true;
			}
		}
		boolean isEnum = false;
		if (hasTransfer == false && fromProperty.getType() != toProperty.getType())
		{
			if (isEnumCopy(fromProperty.getType(), toProperty.getType()) == false)
			{
				return new PropertyCopyDescriptor<S, D>() {
					
					@Override
					public String fromProperty()
					{
						return fromProperty.getName();
					}
					
					@Override
					public String toProperty()
					{
						return toProperty.getName();
					}
					
					@Override
					public void process(S source, D des) throws Exception
					{
						;
					}
				};
			}
			isEnum = true;
		}
		fromProperty.setAccessible(true);
		toProperty.setAccessible(true);
		if (isEnum)
		{
			return new PropertyCopyDescriptor<S, D>() {
				private Class<?> desEnumType = toProperty.getType();
				
				@Override
				public String fromProperty()
				{
					return fromProperty.getName();
				}
				
				@Override
				public String toProperty()
				{
					return toProperty.getName();
				}
				
				@Override
				public void process(S source, D des) throws Exception
				{
					Enum<?> instance = (Enum<?>) fromProperty.get(source);
					if (instance == null)
					{
						return;
					}
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Enum desEnumInstance = Enum.valueOf((Class<Enum>) desEnumType, instance.name());
					toProperty.set(des, desEnumInstance);
				}
			};
		}
		else
		{
			return new PropertyCopyDescriptor<S, D>() {
				
				@Override
				public String fromProperty()
				{
					return fromProperty.getName();
				}
				
				@Override
				public String toProperty()
				{
					return toProperty.getName();
				}
				
				@Override
				public void process(S source, D des) throws IllegalArgumentException, IllegalAccessException
				{
					toProperty.set(des, fromProperty.get(source));
				}
			};
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean isEnumCopy(Class<?> srcType, Class<?> desType)
	{
		if (Enum.class.isAssignableFrom(srcType) == false || Enum.class.isAssignableFrom(desType) == false)
		{
			return false;
		}
		Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) srcType);
		boolean miss = false;
		for (Entry<String, ? extends Enum<?>> entry : allEnumInstances.entrySet())
		{
			try
			{
				Enum.valueOf((Class<Enum>) desType, entry.getKey());
			}
			catch (Exception e)
			{
				miss = true;
				break;
			}
		}
		if (miss)
		{
			return false;
		}
		return true;
	}
	
}
