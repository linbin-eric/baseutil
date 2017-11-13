package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import sun.misc.Unsafe;

public abstract class ReflectCopy<S, D> implements Copy<S, D>
{
	private Class<S>			source;
	private Class<D>			destination;
	private final Copy<S, D>	util;
	
	public ReflectCopy(Class<S> s, Class<D> d)
	{
		source = s;
		destination = d;
		util = new CopyUtilImpl(source, destination);
	}
	
	@SuppressWarnings("unchecked")
	public ReflectCopy()
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		util = new CopyUtilImpl(source, destination);
	}
	
	@SuppressWarnings("unchecked")
	public ReflectCopy(Map<String, String> nameMap)
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		util = new CopyUtilImpl(source, destination, nameMap);
	}
	
	@SuppressWarnings("unchecked")
	public ReflectCopy(String[] pairs)
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		Map<String, String> map = new HashMap<String, String>();
		for (String pair : pairs)
		{
			String[] kv = pair.split(":");
			map.put(kv[0], kv[1]);
		}
		util = new CopyUtilImpl(source, destination, map);
	}
	
	@Override
	public D copy(S src, D desc)
	{
		return util.copy(src, desc);
	}
	
	class CopyUtilImpl implements Copy<S, D>
	{
		private CopyField[] copyFields;
		
		public CopyUtilImpl(Class<S> src, Class<D> des)
		{
			List<CopyField> copyFields = new ArrayList<CopyField>();
			Map<String, Field> srcMap = generate(src);
			Map<String, Field> descMap = generate(des);
			for (Entry<String, Field> each : srcMap.entrySet())
			{
				if (each.getValue().isAnnotationPresent(CopyIgnore.class))
				{
					continue;
				}
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
	
}

abstract class CopyField
{
	private static final Unsafe	unsafe	= ReflectUtil.getUnsafe();
	protected long				srcOff;
	protected long				desOff;
	
	public CopyField(Field srcField, Field desField)
	{
		srcOff = unsafe.objectFieldOffset(srcField);
		desOff = unsafe.objectFieldOffset(desField);
	}
	
	public abstract void copy(Object src, Object des);
	
	public static CopyField build(Field srcField, Field desField)
	{
		Class<?> type = srcField.getType();
		if (type == int.class)
		{
			return new IntField(srcField, desField);
		}
		else if (type == long.class)
		{
			return new LongField(srcField, desField);
		}
		else if (type == boolean.class)
		{
			return new BooleanField(srcField, desField);
		}
		else if (type == float.class)
		{
			return new FloatField(srcField, desField);
		}
		else if (type == double.class)
		{
			return new DoubleField(srcField, desField);
		}
		else if (type == short.class)
		{
			return new ShortField(srcField, desField);
		}
		else if (type == byte.class)
		{
			return new ByteField(srcField, desField);
		}
		else if (type == char.class)
		{
			return new CharField(srcField, desField);
		}
		else
		{
			return new ObjectField(srcField, desField);
		}
		
	}
	
	static class IntField extends CopyField
	{
		
		public IntField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putInt(des, desOff, unsafe.getInt(src, srcOff));
		}
		
	}
	
	static class LongField extends CopyField
	{
		
		public LongField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putLong(des, desOff, unsafe.getLong(src, srcOff));
		}
		
	}
	
	static class ShortField extends CopyField
	{
		
		public ShortField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putShort(des, desOff, unsafe.getShort(src, srcOff));
		}
		
	}
	
	static class ByteField extends CopyField
	{
		
		public ByteField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putByte(des, desOff, unsafe.getByte(src, srcOff));
		}
	}
	
	static class BooleanField extends CopyField
	{
		
		public BooleanField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putBoolean(des, desOff, unsafe.getBoolean(src, srcOff));
		}
		
	}
	
	static class FloatField extends CopyField
	{
		
		public FloatField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putFloat(des, desOff, unsafe.getFloat(src, srcOff));
		}
		
	}
	
	static class CharField extends CopyField
	{
		
		public CharField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putChar(des, desOff, unsafe.getChar(src, srcOff));
		}
		
	}
	
	static class DoubleField extends CopyField
	{
		
		public DoubleField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putDouble(des, desOff, unsafe.getDouble(src, srcOff));
		}
		
	}
	
	static class ObjectField extends CopyField
	{
		
		public ObjectField(Field srcField, Field desField)
		{
			super(srcField, desField);
		}
		
		@Override
		public void copy(Object src, Object des)
		{
			unsafe.putObject(des, desOff, unsafe.getObject(src, srcOff));
		}
		
	}
}
