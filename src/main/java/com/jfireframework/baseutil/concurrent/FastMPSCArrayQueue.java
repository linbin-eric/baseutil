package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import com.jfireframework.baseutil.reflect.UNSAFE;

public class FastMPSCArrayQueue<E> extends Pad4 implements Queue<E>
{
	protected final Object[]	buffer;
	protected final long		mask;
	protected final int			indexShift;
	
	public FastMPSCArrayQueue(int capacity)
	{
		int size = 1;
		int indexShift = 0;
		while (size < capacity && size > 0)
		{
			size <<= 1;
			indexShift++;
		}
		if (size > 0)
		{
			this.indexShift = indexShift;
			mask = size - 1;
			buffer = new Object[size];
		}
		else
		{
			throw new IllegalArgumentException("capacity 无法计算得到其最小的2次方幂");
		}
	}
	
	@Override
	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return consumerIndex == producerIndex;
	}
	
	@Override
	public boolean contains(Object o)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] toArray()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean remove(Object o)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean add(E e)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean offer(E e)
	{
		long pIndex;
		long pLimit = producerIndexLimit;
		do
		{
			pIndex = producerIndex;
			if (pIndex >= pLimit)
			{
				pLimit = consumerIndex + mask + 1;
				if (pIndex >= pLimit)
				{
					return false;
				}
				else
				{
					orderedSetProducerIndexLimit(pLimit);
				}
			}
			if (casProducerIndex(pIndex))
			{
				long offset = ((pIndex & mask) << bufferScaleShift) + bufferOffset;
				UNSAFE.putOrderedObject(buffer, offset, e);
				return true;
			}
		} while (true);
	}
	
	@Override
	public E remove()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		long cIndex = consumerIndex;
		long offset = ((cIndex & mask) << bufferScaleShift) + bufferOffset;
		Object[] buffer = this.buffer;
		Object object = UNSAFE.getObjectVolatile(buffer, offset);
		if (null == object)
		{
			if (cIndex != producerIndex)
			{
				while ((object = UNSAFE.getObjectVolatile(buffer, offset)) == null)
				{
					;
				}
			}
			else
			{
				return null;
			}
		}
		UNSAFE.putObject(buffer, offset, null);
		orderedSetComsumerIndex(cIndex + 1);
		return (E) object;
	}
	
	@Override
	public E element()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public E peek()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
