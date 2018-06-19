package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import com.jfireframework.baseutil.concurrent.MPSCQueue.Node;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.reflect.UnsafeFieldAccess;
import sun.misc.Unsafe;

abstract class HeadLeftPad
{
	public volatile long p1, p2, p3, p4, p5, p6, p7;
	
	public long sumHeadLeftPad()
	{
		return p1 + p2 + p3 + p4 + p5 + p6 + p7;
	}
}

abstract class Head extends HeadLeftPad
{
	public volatile int	leftP;
	protected Node		head;
	public volatile int	rightP;
	
	public int sumHead()
	{
		return leftP + rightP;
	}
}

abstract class HeadRightPad extends Head
{
	public volatile long p11, p21, p31, p41, p51, p61, p71;
	
	public long sumHeadRightPad()
	{
		return p11 + p21 + p31 + p41 + p51 + p61 + p71;
	}
}

abstract class Tail extends HeadRightPad
{
	public volatile int		leftP1;
	protected volatile Node	tail;
	public volatile int		rightP1;
	
	public int sumTail()
	{
		return leftP1 + rightP1;
	}
}

/**
 * Created by 林斌 on 2016/9/10.
 */
public class MPSCQueue<E> extends Tail implements Queue<E>
{
	public volatile long p01, p02, p03, p04, p05, p06, p07;
	
	public long fill()
	{
		return p01 + p02 + p03 + p04 + p05 + p06 + p07;
	}
	
	private static final long	headOff	= UnsafeFieldAccess.getFieldOffset("head", Head.class);
	private static final long	tailOff	= UnsafeFieldAccess.getFieldOffset("tail", Tail.class);
	private static final Unsafe	unsafe	= ReflectUtil.getUnsafe();
	
	public MPSCQueue()
	{
		tail = head = new Node(null);
	}
	
	private void slackSetHead(Node h)
	{
		unsafe.putObject(this, headOff, h);
	}
	
	private boolean casTail(Node expect, Node now)
	{
		return unsafe.compareAndSwapObject(this, tailOff, expect, now);
	}
	
	@SuppressWarnings("unchecked")
	public int drain(E[] array, int limit)
	{
		limit = limit > array.length ? array.length : limit;
		Node p = head, t = tail, pn = p;
		int i = 0;
		for (; pn != t && i < limit; i++)
		{
			p = pn;
			pn = findNext(pn);
			array[i] = (E) pn.value;
		}
		if (i > 0)
		{
			p.forgetNext();
			pn.forgetItem();
			slackSetHead(pn);
		}
		return i;
	}
	
	private Node findNext(Node p)
	{
		Node pn = p.next;
		if (pn != null)
		{
			return pn;
		}
		int spin = 0;
		do
		{
			pn = p.next;
			if (pn != null)
			{
				return pn;
			}
			else if ((spin += 1) > 32)
			{
				spin = 0;
				Thread.yield();
			}
		} while (true);
	}
	
	@SuppressWarnings("unchecked")
	public E poll()
	{
		Node h = head;
		if (h != tail)
		{
			Node hn = h.next;
			if (hn != null)
			{
				Object e = hn.value;
				h.forgetNext();
				hn.forgetItem();
				slackSetHead(hn);
				return (E) e;
			}
			int spin = 0;
			do
			{
				hn = h.next;
				if (hn != null)
				{
					Object e = hn.value;
					h.forgetNext();
					hn.forgetItem();
					slackSetHead(hn);
					return (E) e;
					
				}
				else if ((spin += 1) > 32)
				{
					spin = 0;
					Thread.yield();
				}
			} while (true);
			
		}
		else
		{
			return null;
		}
	}
	
	public boolean offer(E value)
	{
		Node insert = new Node(value);
		Node t = tail;
		if (casTail(t, insert))
		{
			t.slackSetNext(insert);
			return true;
		}
		do
		{
			t = tail;
			if (casTail(t, insert))
			{
				t.slackSetNext(insert);
				return true;
			}
		} while (true);
	}
	
	static class Node
	{
		private Object				value;
		private volatile Node		next;
		private static final long	nextOff		= UnsafeFieldAccess.getFieldOffset("next", Node.class);
		private static final long	valueOff	= UnsafeFieldAccess.getFieldOffset("value", Node.class);
		
		public Node(Object value)
		{
			this.value = value;
		}
		
		public void slackSetNext(Node n)
		{
			unsafe.putOrderedObject(this, nextOff, n);
		}
		
		public void forgetNext()
		{
			unsafe.putObject(this, nextOff, this);
		}
		
		public void forgetItem()
		{
			unsafe.putObject(this, valueOff, this);
		}
	}
	
	@Override
	public int size()
	{
		int count = 0;
		Node h = head;
		do
		{
			Node hn = h.next;
			if (hn != null)
			{
				count += 1;
				h = hn;
			}
			else
			{
				break;
			}
		} while (true);
		return count;
	}
	
	@Override
	public boolean isEmpty()
	{
		return head == tail;
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
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		throw new UnsupportedOperationException();
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
		Node h = head, hn = h, t = tail;
		for (; hn != t;)
		{
			h = hn;
			hn = findNext(hn);
		}
		h.forgetNext();
		hn.forgetItem();
		slackSetHead(hn);
	}
	
	@Override
	public boolean add(E e)
	{
		offer(e);
		return true;
	}
	
	@Override
	public E remove()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E element()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E peek()
	{
		throw new UnsupportedOperationException();
	}
	
}
