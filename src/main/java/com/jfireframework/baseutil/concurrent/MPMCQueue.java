package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import com.jfireframework.baseutil.reflect.UNSAFE;

public class MPMCQueue<E> implements Queue<E>
{
    private CpuCachePadingRefence<Node<E>> head;
    private CpuCachePadingRefence<Node<E>> tail;
    private final boolean                  fair;
    private Sync<E>                        sync = new Sync<E>() {
                                                    
                                                    @Override
                                                    protected E get()
                                                    {
                                                        return poll();
                                                    }
                                                };
    
    public MPMCQueue()
    {
        this(false);
    }
    
    public MPMCQueue(boolean fair)
    {
        Node<E> n = new Node<E>(null);
        head = new CpuCachePadingRefence<MPMCQueue.Node<E>>(n);
        tail = new CpuCachePadingRefence<MPMCQueue.Node<E>>(n);
        this.fair = fair;
    }
    
    private static class Node<E>
    {
        // 经过JMH测试，在E是volatile的时候，并且有clear操作的情况下测试性能最高。无法解释为什么操作少了反而性能下降。
        private volatile E        value;
        private volatile Node<E>  next;
        private static final long valueOffset = UNSAFE.getFieldOffset("value", Node.class);
        private static final long nextOffset  = UNSAFE.getFieldOffset("next", Node.class);
        
        public Node(E value)
        {
            UNSAFE.putObject(this, valueOffset, value);
        }
        
        public void orderSetNext(Node<E> next)
        {
            UNSAFE.putOrderedObject(this, nextOffset, next);
        }
        
        public E clear()
        {
            E origin = value;
            UNSAFE.putObject(this, Node.valueOffset, null);
            return origin;
        }
        
        public void unlink()
        {
            UNSAFE.putObject(this, Node.nextOffset, null);
        }
    }
    
    @Override
    public void clear()
    {
    }
    
    @Override
    public boolean offer(E o)
    {
        if (o == null)
        {
            throw new NullPointerException();
        }
        Node<E> insert_node = new Node<E>(o);
        Node<E> old = tail.get();
        if (tail.compareAndSwap(old, insert_node))
        {
            old.orderSetNext(insert_node);
            return true;
        }
        do
        {
            old = tail.get();
            if (tail.compareAndSwap(old, insert_node))
            {
                old.orderSetNext(insert_node);
                return true;
            }
        } while (true);
        
    }
    
    public void offerAndSignal(E o)
    {
        offer(o);
        sync.signal();
    }
    
    public E mastPull()
    {
        startFromHead: //
        for (Node<E> h = head.get(), next = h.next; //
        ; //
                h = head.get(), next = h.next)
        {
            if (next == null)
            {
                for (next = h.next; h == head.get(); next = h.next)
                {
                    if (next == null && (next = h.next) == null)
                    {
                        continue;
                    }
                    else
                    {
                    }
                    if (head.compareAndSwap(h, next))
                    {
                        h.unlink();
                        return next.clear();
                    }
                    else
                    {
                        continue startFromHead;
                    }
                }
            }
            else
            {
                if (head.compareAndSwap(h, next))
                {
                    h.unlink();
                    return next.clear();
                }
                else
                {
                }
            }
        }
    }
    
    @Override
    public E poll()
    {
        {
            Node<E> h = head.get();
            Node<E> next = h.next;
            if (next != null)
            {
                if (head.compareAndSwap(h, next))
                {
                    h.unlink();
                    return next.clear();
                }
            }
            else if (h == tail.get())
            {
                return null;
            }
        }
        startFromHead: //
        for (Node<E> h = head.get(), next = h.next, t = tail.get(); //
                h != t || h != (t = tail.get()); //
                h = head.get(), next = h.next)
        {
            if (next == null)
            {
                for (next = h.next; h == head.get(); next = h.next)
                {
                    if (next == null && (next = h.next) == null)
                    {
                        continue;
                    }
                    else
                    {
                    }
                    if (head.compareAndSwap(h, next))
                    {
                        h.unlink();
                        return next.clear();
                    }
                    else
                    {
                        continue startFromHead;
                    }
                }
            }
            else
            {
                if (head.compareAndSwap(h, next))
                {
                    h.unlink();
                    return next.clear();
                }
                else
                {
                }
            }
        }
        return null;
    }
    
    private E fairTake(long time, TimeUnit unit)
    {
        if (sync.hasWaiters() == false)
        {
            E result = poll();
            if (result == null)
            {
                if (time == -1)
                {
                    return enqueueAndWait();
                }
                else
                {
                    return enqueueAndWait(time, unit);
                }
            }
            else
            {
                return result;
            }
        }
        else
        {
            if (time == -1)
            {
                return enqueueAndWait();
            }
            else
            {
                return enqueueAndWait(time, unit);
            }
        }
    }
    
    private E enqueueAndWait(long time, TimeUnit unit)
    {
        return sync.take(time, unit);
    }
    
    private E enqueueAndWait()
    {
        return sync.take();
    }
    
    private E unfairTake(long time, TimeUnit unit)
    {
        E result = poll();
        if (result == null)
        {
            if (time == -1)
            {
                return enqueueAndWait();
            }
            else
            {
                return enqueueAndWait(time, unit);
            }
        }
        else
        {
            return result;
        }
    }
    
    /**
     * 阻塞的获取一个元素。如果没有元素，则一直阻塞等待
     * 
     * @return
     */
    public E take()
    {
        if (fair)
        {
            return fairTake(-1, null);
        }
        else
        {
            return unfairTake(-1, null);
        }
    }
    
    public E take(long time, TimeUnit unit)
    {
        if (fair)
        {
            return fairTake(time, unit);
        }
        else
        {
            return unfairTake(time, unit);
        }
    }
    
    @Override
    public int size()
    {
        return -1;
    }
    
    @Override
    public boolean isEmpty()
    {
        return head.value == tail.value;
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
    public boolean add(E e)
    {
        offer(e);
        return true;
    }
    
    @Override
    public E remove()
    {
        return mastPull();
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
