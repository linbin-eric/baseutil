package com.jfireframework.baseutil.concurrent;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.reflect.UnsafeFieldAccess;
import sun.misc.Unsafe;

abstract class PadFor128Bit
{
	// 128长度的缓存行，要进行填充，需要16个byte。
	long	p0, p1, p2, p3, p4, p5, p6, p7;
	long	p11, p12, p13, p14, p15, p16, p17;
}

abstract class ProducerIndex extends PadFor128Bit
{
	volatile long producerIndex;
}

abstract class Pad2 extends ProducerIndex
{
	public long	p0, p1, p2, p3, p4, p5, p6, p7;
	public long	p11, p12, p13, p14, p15, p16, p17;
}

abstract class Core<E> extends Pad2 implements Queue<E>
{
	protected final E[]		buffer;
	protected final int		mask;
	protected final int[]	availableBuffers;
	protected final int		indexShift;
	
	@SuppressWarnings("unchecked")
	Core(int capacity)
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
			buffer = (E[]) new Object[size];
			availableBuffers = new int[size];
			Arrays.fill(availableBuffers, -1);
		}
		else
		{
			throw new IllegalArgumentException("capacity 无法计算得到其最小的2次方幂");
		}
	}
	
}

abstract class Pad3<E> extends Core<E>
{
	long	p0, p1, p2, p3, p4, p5, p6, p7;
	long	p11, p12, p13, p14, p15, p16, p17;
	
	Pad3(int capacity)
	{
		super(capacity);
	}
}

abstract class ComsumerIndex<E> extends Pad3<E>
{
	long consumerIndex;
	
	ComsumerIndex(int capacity)
	{
		super(capacity);
	}
	
}

abstract class Pad4<E> extends ComsumerIndex<E>
{
	long	p0, p1, p2, p3, p4, p5, p6, p7;
	long	p11, p12, p13, p14, p15, p16, p17;
	
	Pad4(int capacity)
	{
		super(capacity);
	}
	
}

abstract class ProducerIndexLimit<E> extends Pad4<E>
{
	volatile long producerIndexLimit = 0;
	
	ProducerIndexLimit(int capacity)
	{
		super(capacity);
	}
}

abstract class Pad5<E> extends ProducerIndexLimit<E>
{
	long	p0, p1, p2, p3, p4, p5, p6, p7;
	long	p11, p12, p13, p14, p15, p16, p17;
	
	Pad5(int capacity)
	{
		super(capacity);
	}
	
}

abstract class AccessInfo<E> extends Pad5<E>
{
	
	static Unsafe		unsafe						= ReflectUtil.getUnsafe();
	static final long	consumerIndexAddress		= UnsafeFieldAccess.getFieldOffset("consumerIndex", ComsumerIndex.class);
	static final long	producerIndexAddress		= UnsafeFieldAccess.getFieldOffset("producerIndex", ProducerIndex.class);
	static final long	producerIndexLimitAddress	= UnsafeFieldAccess.getFieldOffset("producerIndexLimit", ProducerIndexLimit.class);
	static final long	availableBufferOffset		= unsafe.arrayBaseOffset(new int[0].getClass());
	static final long	bufferOffset				= unsafe.arrayBaseOffset(Object[].class);
	static final long	availableBufferScaleShift;
	static final long	bufferScaleShift;
	
	static
	{
		int availableBufferScale = unsafe.arrayIndexScale(new int[0].getClass());
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
		int bufferScale = unsafe.arrayIndexScale(Object[].class);
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
	
	AccessInfo(int capacity)
	{
		super(capacity);
	}
	
	final long getConsumerIndexVolatile()
	{
		return unsafe.getLongVolatile(this, consumerIndexAddress);
	}
	
	boolean isAvailable(long index)
	{
		int flag = (int) (index >>> indexShift);
		long address = ((index & mask) << availableBufferScaleShift) + availableBufferOffset;
		return unsafe.getIntVolatile(availableBuffers, address) == flag;
	}
	
	boolean isAvailable(long address, int flag)
	{
		return unsafe.getIntVolatile(availableBuffers, address) == flag;
	}
	
	void setAvailable(long index)
	{
		int flag = (int) (index >>> indexShift);
		long address = ((index & mask) << availableBufferScaleShift) + availableBufferOffset;
		unsafe.putOrderedInt(availableBuffers, address, flag);
	}
	
	/**
	 * 获取下一个可以使用的生产者下标
	 * 
	 * @return
	 */
	long nextProducerIndex()
	{
		long producerIndexAddress = AccessInfo.producerIndexAddress;
		int mask = this.mask;
		long pLimit = producerIndexLimit;
		long pIndex = producerIndex;
		if (pIndex < pLimit)
		{
			if (unsafe.compareAndSwapLong(this, producerIndexAddress, pIndex, pIndex + 1))
			{
				return pIndex;
			}
		}
		boolean limitChange = false;
		do
		{
			pIndex = producerIndex;
			if (pIndex < pLimit)
			{
				if (unsafe.compareAndSwapLong(this, producerIndexAddress, pIndex, pIndex + 1))
				{
					if (limitChange)
					{
						setProducerIndexLimit(pLimit);
					}
					return pIndex;
				}
				pIndex = producerIndex;
				if (pIndex >= pLimit)
				{
					pLimit = getConsumerIndexVolatile() + mask + 1;
					limitChange = true;
					if (pIndex >= pLimit)
					{
						// 队列已满
						return -1;
					}
				}
			}
			else
			{
				pLimit = getConsumerIndexVolatile() + mask + 1;
				if (pIndex >= pLimit)
				{
					// 队列已满
					return -1;
				}
				else
				{
					limitChange = true;
				}
			}
		} while (true);
	}
	
	@SuppressWarnings("unchecked")
	E get(long index)
	{
		long address = ((index & mask) << bufferScaleShift) + bufferOffset;
		return (E) unsafe.getObject(buffer, address);
	}
	
	void set(E value, long index)
	{
		long address = ((index & mask) << bufferScaleShift) + bufferOffset;
		unsafe.putObject(buffer, address, value);
	}
	
	void setProducerIndexLimit(long limit)
	{
		unsafe.putOrderedLong(this, producerIndexLimitAddress, limit);
	}
	
	void waitUnitlAvailable(long index)
	{
		int flag = (int) (index >>> indexShift);
		long address = ((index & mask) << availableBufferScaleShift) + availableBufferOffset;
		if (isAvailable(address, flag) == false)
		{
			while (isAvailable(address, flag) == false)
			{
				Thread.yield();
			}
		}
	}
}

public class MPSCArrayQueue<E> extends AccessInfo<E> implements Queue<E>
{
	
	public MPSCArrayQueue(int capacity)
	{
		super(capacity);
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
			E e = get(i);
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
			
			@Override
			public E next()
			{
				if (hasNext())
				{
					waitUnitlAvailable(index);
					return get(index);
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
	
	@Override
	public E poll()
	{
		long pIndex = producerIndex;
		long cIndex = this.consumerIndex;
		if (pIndex == cIndex)
		{
			return null;
		}
		int flag = (int) (cIndex >>> indexShift);
		long address = ((cIndex & mask) << availableBufferScaleShift) + availableBufferOffset;
		if (isAvailable(address, flag) == false)
		{
			while (isAvailable(address, flag) == false)
			{
				Thread.yield();
			}
		}
		E e = get(cIndex);
		set(null, cIndex);
		this.consumerIndex = cIndex+1;
		return e;
	}
	
	@Override
	public E element()
	{
		return peek();
	}
	
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
		if (isAvailable(address, flag) == false)
		{
			while (isAvailable(address, flag) == false)
			{
				Thread.yield();
			}
		}
		E e = get(consumerIndex);
		return e;
	}
	
}
