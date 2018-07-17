package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import com.jfireframework.baseutil.concurrent.SpscQueue.Node;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.reflect.UNSAFE;
import sun.misc.Unsafe;

abstract class HeadLeftPad_spsc
{
	public volatile long p1, p2, p3, p4, p5, p6, p7;
	
	public long sumHeadLeftPad()
	{
		return p1 + p2 + p3 + p4 + p5 + p6 + p7;
	}
}

abstract class Head_spsc extends HeadLeftPad_spsc
{
	public volatile int	leftP;
	protected Node		head;
	public volatile int	rightP;
	
	public int sumHead()
	{
		return leftP + rightP;
	}
}

abstract class HeadRightPad_spsc extends Head_spsc
{
	public volatile long p11, p21, p31, p41, p51, p61, p71;
	
	public long sumHeadRightPad()
	{
		return p11 + p21 + p31 + p41 + p51 + p61 + p71;
	}
}

abstract class Tail_spsc extends HeadRightPad_spsc
{
	public volatile int	leftP1;
	protected Node		tail;
	public volatile int	rightP1;
	
	public int sumTail()
	{
		return leftP1 + rightP1;
	}
}

public class SpscQueue<E> extends Tail_spsc implements Queue<E>
{
	public volatile long p01, p02, p03, p04, p05, p06, p07;
	
	public long fill()
	{
		return p01 + p02 + p03 + p04 + p05 + p06 + p07;
	}
	
	public SpscQueue()
	{
		Node init = new Node(null);
		head = tail = init;
	}
	
	public boolean offer(E e)
	{
		Node insert = new Node(e), t = tail;
		t.next = insert;
		tail = insert;
		return true;
	}
	
	public E poll()
	{
		Node h = head, hn = h.next;
		if (hn != null)
		{
			@SuppressWarnings("unchecked")
			E e = (E) hn.item;
			h.forgetNext();
			hn.forgetItem();
			head = hn;
			return e;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public int drain(E[] array, int limit)
	{
		Node p = head, pn = p.next;
		int i = 0;
		for (; i < limit && (pn = p.next) != null; i++)
		{
			array[i] = (E) pn.item;
			p = pn;
		}
		if (i > 0)
		{
			p.forgetItem();
			head = p;
		}
		return i;
	}
	
	static class Node
	{
		Object						item;
		volatile Node				next;
		private static final long	offset	= UNSAFE.getFieldOffset("next", Node.class);
		private static final Unsafe	unsafe	= ReflectUtil.getUnsafe();
		
		public Node(Object item)
		{
			this.item = item;
		}
		
		public void forgetNext()
		{
			unsafe.putObject(this, offset, this);
		}
		
		public void forgetItem()
		{
			item = this;
		}
	}
	
	@Override
	public boolean isEmpty()
	{
		return head.next == null;
	}
	
	@Override
	public boolean contains(Object o)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<E> iterator()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object[] toArray()
	{
		Object[] array = new Object[size()];
		Node h = head;
		for (int i = 0; i < array.length; i++)
		{
			h = h.next;
			array[i] = h.item;
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		int size = size();
		size = size > a.length ? a.length : size;
		Node h = head;
		for (int i = 0; i < a.length; i++)
		{
			h = h.next;
			a[i] = (T) h.item;
		}
		return a;
	}
	
	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		for (E e : c)
		{
			offer(e);
		}
		return true;
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
		Node h = head, t = tail, hn;
		while ((hn = h.next) != t)
		{
			h = hn;
		}
		h.forgetNext();
		t.forgetItem();
		head = t;
	}
	
	@Override
	public boolean add(E e)
	{
		return offer(e);
	}
	
	@Override
	public E remove()
	{
		return poll();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E element()
	{
		Node hn = head.next;
		return (E) (hn == null ? null : hn.item);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		Node hn = head.next;
		return (E) (hn == null ? null : hn.item);
	}
	
	@Override
	public int size()
	{
		int i = 0;
		Node h = head;
		while ((h = h.next) != null)
		{
			i += 1;
		}
		return i;
	}
	
}
