package com.jfireframework.baseutil.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import com.jfireframework.baseutil.reflect.UNSAFE;

public class XMPSCLinkQueue<E> implements Queue<E>
{
    
    static final int  size                     = 2048;
    static final int  BUFFER_ARRAY_OFFSET      = UNSAFE.arrayBaseOffset(Object[].class);
    static final int  BUFFER_ARRAY_SCALE_SHIFT = UNSAFE.arrayIndexScale(Object[].class) == 8 ? 3 : 2;
    static final long FLAG_OFFSET              = UNSAFE.getFieldOffset("flag", XMPSCLinkQueue.class);
    
    static class Segment
    {
        final Object[]    buffer;
        volatile Segment  next;
        int               readPosi          = 0;
        volatile int      writePosi         = 0;
        static final long WRITE_POSI_OFFSET = UNSAFE.getFieldOffset("writePosi", Segment.class);
        
        public Segment()
        {
            buffer = new Object[size];
        }
        
        int nextWritePosi()
        {
            int posi;
            do
            {
                posi = writePosi;
                if (posi >= size)
                {
                    return -1;
                }
            } while (UNSAFE.compareAndSwapInt(this, WRITE_POSI_OFFSET, posi, posi + 1) == false);
            return posi;
        }
        
        void orderSet(Object value, int posi)
        {
            long offset = ((long) posi << BUFFER_ARRAY_SCALE_SHIFT) + BUFFER_ARRAY_OFFSET;
            UNSAFE.putOrderedObject(buffer, offset, value);
        }
        
        Object getVolatile(int posi)
        {
            long offset = ((long) posi << BUFFER_ARRAY_SCALE_SHIFT) + BUFFER_ARRAY_OFFSET;
            return UNSAFE.getObjectVolatile(buffer, offset);
        }
        
        void setNull(int posi)
        {
            long offset = ((long) posi << BUFFER_ARRAY_SCALE_SHIFT) + BUFFER_ARRAY_OFFSET;
            UNSAFE.putObject(buffer, offset, null);
        }
    }
    
    Segment          head;
    volatile Segment tail;
    volatile int     flag = idle;
    static final int busy = 1;
    static final int idle = 2;
    
    public XMPSCLinkQueue()
    {
        head = tail = new Segment();
    }
    
    public boolean offer(E e)
    {
        Segment tail = this.tail;
        Segment segment = tail;
        int writePosi = segment.nextWritePosi();
        if (writePosi != -1)
        {
            segment.orderSet(e, writePosi);
            return true;
        }
        do
        {
            if (casFlag())
            {
                if (segment == (tail = this.tail))
                {
                    this.tail = tail = segment = segment.next = new Segment();
                }
                else
                {
                    segment = tail;
                }
                flag = idle;
            }
            else
            {
                if (segment == (tail = this.tail))
                {
                    continue;
                }
                else
                {
                    segment = tail;
                }
            }
        } while ((writePosi = segment.nextWritePosi()) == -1);
        segment.orderSet(e, writePosi);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public E poll()
    {
        Segment head = this.head;
        int readPosi = head.readPosi;
        if (readPosi == size)
        {
            if (head.next == null)
            {
                return null;
            }
            this.head = head = head.next;
            readPosi = head.readPosi;
        }
        E e = (E) head.getVolatile(readPosi);
        if (e == null)
        {
            if (readPosi != head.writePosi)
            {
                while ((e = (E) head.getVolatile(readPosi)) == null)
                {
                }
            }
            else
            {
                return null;
            }
        }
        head.setNull(readPosi);
        head.readPosi = readPosi + 1;
        return e;
    }
    
    boolean casFlag()
    {
        int now = flag;
        return now == idle && UNSAFE.compareAndSwapInt(this, FLAG_OFFSET, idle, busy);
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
        return tail.readPosi == tail.writePosi;
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
    public E remove()
    {
        // TODO Auto-generated method stub
        return null;
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
