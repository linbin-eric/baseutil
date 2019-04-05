package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import com.jfireframework.baseutil.concurrent.MPSCLinkedQueue.Node;
import com.jfireframework.baseutil.reflect.UNSAFE;

abstract class Pad1
{
    public long p11, p21, p31, p41, p51, p61, p71;
    
    public long sumHeadRightPad()
    {
        return p11 + p21 + p31 + p41 + p51 + p61 + p71;
    }
}

abstract class Tail extends Pad1
{
    protected volatile Node   tail;
    private static final long tailOff = UNSAFE.getFieldOffset("tail", Tail.class);
    
    Node casTail(Node insert)
    {
        Node local;
        do
        {
            local = tail;
        } while (UNSAFE.compareAndSwapObject(this, tailOff, local, insert) == false);
        return local;
    }
}

abstract class PadTail extends Tail
{
    public long p01, p02, p03, p04, p05, p06, p07;
    
    public long fill()
    {
        return p01 + p02 + p03 + p04 + p05 + p06 + p07;
    }
}

/**
 * Created by 林斌 on 2016/9/10.
 */
public class MPSCLinkedQueue<E> extends PadTail implements Queue<E>
{
    
    protected Node head;
    
    public MPSCLinkedQueue()
    {
        tail = head = new Node(null);
    }
    
    private void slackSetHead(Node h)
    {
        head = h;
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
        Node hn = h.next;
        if (hn != null)
        {
            Object e = hn.getValueAndNullIt();
            h.forgetNext();
            slackSetHead(hn);
            return (E) e;
        }
        else
        {
            if (h != tail)
            {
                while ((hn = h.next) == null)
                {
                }
                Object e = hn.getValueAndNullIt();
                h.forgetNext();
                slackSetHead(hn);
                return (E) e;
            }
            else
            {
                return null;
            }
        }
    }
    
    public boolean offer(E value)
    {
        Node insert = new Node(value);
        Node pred = casTail(insert);
        pred.slackSetNext(insert);
        return true;
    }
    
    static class Node
    {
        private Object            value;
        private volatile Node     next;
        private static final long nextOff = UNSAFE.getFieldOffset("next", Node.class);
        
        public Node(Object value)
        {
            this.value = value;
        }
        
        public Object getValueAndNullIt()
        {
            Object e = value;
            value = null;
            return e;
        }
        
        public void slackSetNext(Node n)
        {
            UNSAFE.putOrderedObject(this, nextOff, n);
        }
        
        public void forgetNext()
        {
            UNSAFE.putObject(this, nextOff, this);
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
        hn.getValueAndNullIt();
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
