package com.jfireframework.baseutil.concurrent;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import com.jfireframework.baseutil.reflect.UNSAFE;

abstract class PadFor64Bit
{
	// 64长度的缓存行，要进行填充，需要8个byte。
	long p1, p2, p3, p4, p5, p6, p7;
	
	public static long noHuop(PadFor64Bit instance)
	{
		return instance.p1 + instance.p2 + instance.p3 + instance.p4 + instance.p5 + instance.p6 + instance.p7;
	}
}

abstract class ProducerIndex extends PadFor64Bit
{
	volatile long				producerIndex;
	private static final long	OFFSET	= UNSAFE.getFieldOffset("producerIndex", ProducerIndex.class);
	
	boolean casProducerIndex(long index)
	{
		return UNSAFE.compareAndSwapLong(this, OFFSET, index, index + 1);
	}
}

abstract class Pad2 extends ProducerIndex
{
	public long p1, p2, p3, p4, p5, p6, p7;
	
	public static long noHuop(Pad2 instance)
	{
		return instance.p1 + instance.p2 + instance.p3 + instance.p4 + instance.p5 + instance.p6 + instance.p7;
	}
}

abstract class ComsumerIndex extends Pad2
{
	volatile long				consumerIndex;
	private static final long	OFFSET	= UNSAFE.getFieldOffset("consumerIndex", ComsumerIndex.class);
	
	void orderedSetComsumerIndex(long index)
	{
		UNSAFE.putOrderedLong(this, OFFSET, index);
	}
}

abstract class Pad3 extends ComsumerIndex
{
	long p1, p2, p3, p4, p5, p6, p7;
	
	public static long noHuop(Pad3 instance)
	{
		return instance.p1 + instance.p2 + instance.p3 + instance.p4 + instance.p5 + instance.p6 + instance.p7;
	}
}

abstract class ProducerIndexLimit extends Pad3
{
	volatile long				producerIndexLimit			= 0;
	private static final long	OFFSET	= UNSAFE.getFieldOffset("producerIndexLimit", ProducerIndexLimit.class);
	
	void orderedSetProducerIndexLimit(long limit)
	{
		UNSAFE.putOrderedLong(this, OFFSET, limit);
	}
}

abstract class Pad4 extends ProducerIndexLimit
{
	long				p1, p2, p3, p4, p5, p6, p7;
	
	static final int	availableBufferOffset	= UNSAFE.arrayBaseOffset(new int[0].getClass());
	static final int	bufferOffset			= UNSAFE.arrayBaseOffset(Object[].class);
	static final int	availableBufferScaleShift;
	static final int	bufferScaleShift;
	
	static
	{
		int availableBufferScale = UNSAFE.arrayIndexScale(new int[0].getClass());
		if (availableBufferScale == 4)
		{
			availableBufferScaleShift = 2;
		}
		else if (availableBufferScale == 8)
		{
			availableBufferScaleShift = 3;
		}
		else
		{
			throw new IllegalArgumentException();
		}
		int bufferScale = UNSAFE.arrayIndexScale(Object[].class);
		if (bufferScale == 4)
		{
			bufferScaleShift = 2;
		}
		else if (bufferScale == 8)
		{
			bufferScaleShift = 3;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	public static long noHuop(Pad4 instance)
	{
		return instance.p1 + instance.p2 + instance.p3 + instance.p4 + instance.p5 + instance.p6 + instance.p7;
	}
}

public class MPSCArrayQueue<E> extends Pad4 implements Queue<E>
{
	
	public MPSCArrayQueue(int capacity)
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
			availableBuffers = new int[size];
			Arrays.fill(availableBuffers, -1);
		}
		else
		{
			throw new IllegalArgumentException("capacity 无法计算得到其最小的2次方幂");
		}
	}
	
	protected final Object[]	buffer;
	protected final int			mask;
	protected final int[]		availableBuffers;
	protected final int			indexShift;
	
	boolean isAvailable(long address, int flag, int[] availableBuffers)
	{
		return UNSAFE.getIntVolatile(availableBuffers, address) == flag;
	}
	
	void setAvailable(long index)
	{
		int flag = (int) (index >>> indexShift);
		long address = ((index & mask) << availableBufferScaleShift) + availableBufferOffset;
		UNSAFE.putOrderedInt(availableBuffers, address, flag);
	}
	
	/**
	 * 获取下一个可以使用的生产者下标
	 * 
	 * @return
	 */
	long nextProducerIndex()
	{
		long pIndex = producerIndex;
		long pLimit = producerIndexLimit;
		if (pIndex < pLimit)
		{
			if (casProducerIndex(pIndex))
			{
				return pIndex;
			}
		}
		do
		{
			pIndex = producerIndex;
			if (pIndex < pLimit)
			{
				if (casProducerIndex(pIndex))
				{
					return pIndex;
				}
			}
			else
			{
				pLimit = producerIndexLimit = consumerIndex + mask + 1;
				if (pIndex >= producerIndexLimit)
				{
					// 队列已满
					return -1;
				}
				else
				{
					if (casProducerIndex(pIndex))
					{
						return pIndex;
					}
				}
			}
		} while (true);
	}
	
	Object get(long index)
	{
		long address = ((index & mask) << bufferScaleShift) + bufferOffset;
		return UNSAFE.getObject(buffer, address);
	}
	
	void set(Object value, long index)
	{
		long address = ((index & mask) << bufferScaleShift) + bufferOffset;
		UNSAFE.putObject(buffer, address, value);
	}
	
	Object getAndSetNull(long index)
	{
		long address = ((index & mask) << bufferScaleShift) + bufferOffset;
		Object result = UNSAFE.getObject(buffer, address);
		UNSAFE.putObject(buffer, address, null);
		return result;
	}
	
	void waitUnitlAvailable(long index)
	{
		int flag = (int) (index >>> indexShift);
		long address = ((index & mask) << availableBufferScaleShift) + availableBufferOffset;
		int[] availableBuffers = this.availableBuffers;
		if (isAvailable(address, flag, availableBuffers) == false)
		{
			while (isAvailable(address, flag, availableBuffers) == false)
			{
				Thread.yield();
			}
		}
	}
	
	@Override
	public int size()
	{
		long consumerIndex = this.consumerIndex;
		long producerIndex = this.producerIndex;
		return (int) (producerIndex - consumerIndex);
	}
	
	@Override
	public boolean isEmpty()
	{
		long consumerIndex = this.consumerIndex;
		long producerIndex = this.producerIndex;
		return consumerIndex == producerIndex;
	}
	
	@Override
	public boolean contains(Object o)
	{
		long pIndex = producerIndex;
		long consumerIndex = this.consumerIndex;
		if (pIndex == consumerIndex)
		{
			return false;
		}
		for (long i = consumerIndex; i < pIndex; i++)
		{
			waitUnitlAvailable(i);
			@SuppressWarnings("unchecked")
			E e = (E) get(i);
			if (o == null ? e == null : o.equals(e))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		final long pIndex = producerIndex;
		final long consumerIndex = this.consumerIndex;
		Iterator<E> iterator = new Iterator<E>() {
			long index = consumerIndex;
			
			@Override
			public boolean hasNext()
			{
				return index < pIndex;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public E next()
			{
				if (hasNext())
				{
					waitUnitlAvailable(index);
					return (E) get(index);
				}
				else
				{
					throw new NoSuchElementException();
				}
			}
			
			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
		return iterator;
	}
	
	@Override
	public Object[] toArray()
	{
		int size = size();
		Iterator<E> iterator = iterator();
		Object[] array = new Object[size];
		for (int i = 0; i < array.length; i++)
		{
			array[i] = iterator.next();
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		int size = size();
		if (a.length >= size)
		{
			Iterator<E> iterator = iterator();
			for (int i = 0; i < size; i++)
			{
				a[i] = (T) iterator.next();
			}
			return a;
		}
		else
		{
			a = (T[]) Array.newInstance(a.getClass(), size);
			Iterator<E> iterator = iterator();
			for (int i = 0; i < size; i++)
			{
				a[i] = (T) iterator.next();
			}
			return a;
		}
	}
	
	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		Iterator<E> iterator = iterator();
		while (iterator.hasNext())
		{
			if (c.contains(iterator.next()) == false)
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear()
	{
		long pIndex = producerIndex;
		long cIndex = this.consumerIndex;
		if (pIndex == cIndex)
		{
			return;
		}
		for (long index = cIndex; index < pIndex; index++)
		{
			waitUnitlAvailable(index);
			set(null, index);
		}
		this.consumerIndex = pIndex;
	}
	
	@Override
	public boolean add(E e)
	{
		return offer(e);
	}
	
	@Override
	public boolean offer(E e)
	{
		long index = nextProducerIndex();
		if (index == -1)
		{
			return false;
		}
		set(e, index);
		setAvailable(index);
		return true;
	}
	
	@Override
	public E remove()
	{
		return poll();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		long cIndex = this.consumerIndex;
		int flag = (int) (cIndex >>> indexShift);
		long address = ((cIndex & mask) << availableBufferScaleShift) + availableBufferOffset;
		final int[] availableBuffers = this.availableBuffers;
		if (isAvailable(address, flag, availableBuffers) == false)
		{
			if (cIndex == producerIndex)
			{
				return null;
			}
			while (isAvailable(address, flag, availableBuffers) == false)
			{
				// assert cIndex < consumerLimit;
				Thread.yield();
			}
		}
		E e = (E) getAndSetNull(cIndex);
		orderedSetComsumerIndex(cIndex + 1);
		return e;
	}
	
	@Override
	public E element()
	{
		return peek();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		long pIndex = producerIndex;
		long consumerIndex = this.consumerIndex;
		if (pIndex == consumerIndex)
		{
			return null;
		}
		int flag = (int) (consumerIndex >>> indexShift);
		long address = ((consumerIndex & mask) << availableBufferScaleShift) + availableBufferOffset;
		int[] availableBuffers = this.availableBuffers;
		if (isAvailable(address, flag, availableBuffers) == false)
		{
			while (isAvailable(address, flag, availableBuffers) == false)
			{
				Thread.yield();
			}
		}
		E e = (E) get(consumerIndex);
		return e;
	}
	
}
