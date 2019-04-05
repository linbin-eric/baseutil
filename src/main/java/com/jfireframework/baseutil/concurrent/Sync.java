package com.jfireframework.baseutil.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.jfireframework.baseutil.reflect.UNSAFE;

public abstract class Sync<E>
{
    private volatile Node     head;
    private volatile Node     tail;
    private static final long tailOffset = UNSAFE.getFieldOffset("tail", Sync.class);
    private static final int  WAITING    = 1;
    private static final int  CANCELED   = 2;
    
    static class Node
    {
        private Node              prev;
        private volatile Thread   successor;
        private volatile int      status;
        private static final long statusOffset    = UNSAFE.getFieldOffset("status", Node.class);
        private static final long successorOffset = UNSAFE.getFieldOffset("successor", Node.class);
        
        public Node()
        {
            UNSAFE.putInt(this, statusOffset, WAITING);
        }
        
        public void relaxSetSuccessor(Thread next)
        {
            UNSAFE.putOrderedObject(this, successorOffset, next);
        }
        
        public void clean()
        {
            prev = null;
            UNSAFE.putObject(this, successorOffset, null);
        }
    }
    
    public Sync()
    {
        head = tail = new Node();
    }
    
    public boolean hasWaiters()
    {
        return head != tail;
    }
    
    private Node enqueue()
    {
        Thread t = Thread.currentThread();
        Node insert = new Node();
        Node pred = tail;
        insert.prev = pred;
        if (UNSAFE.compareAndSwapObject(this, tailOffset, pred, insert))
        {
            // pred.nextWaiter = t;
            pred.relaxSetSuccessor(t);
            return insert;
        }
        for (;;)
        {
            pred = tail;
            insert.prev = pred;
            if (UNSAFE.compareAndSwapObject(this, tailOffset, pred, insert))
            {
                pred.relaxSetSuccessor(t);
                // pred.nextWaiter = t;
                return insert;
            }
        }
    }
    
    public void signal()
    {
        Node h = head;
        unparkSuccessor(h);
    }
    
    /**
     * 获取独占资源
     * 
     * @return
     */
    protected abstract E get();
    
    public E take(long time, TimeUnit unit)
    {
        E result;
        Node self = enqueue();
        Node pred = self.prev;
        Node h;
        long nanos = unit.toNanos(time);
        long t0 = System.nanoTime();
        do
        {
            if (pred == (h = head))
            {
                result = get();
                if (result == null)
                {
                    if (nanos < 1000)
                    {
                        for (int i = 0; i < 1000; i++)
                        {
                        }
                    }
                    else
                    {
                        LockSupport.parkNanos(nanos);
                    }
                    nanos -= System.nanoTime() - t0;
                    if (nanos < 0)
                    {
                        cancel(self);
                        return null;
                    }
                    t0 = System.nanoTime();
                }
                else
                {
                    head = self;
                    unparkSuccessor(self);
                    return result;
                }
            }
            else
            {
                if (nanos < 1000)
                {
                    for (int i = 0; i < 1000; i++)
                    {
                    }
                }
                else
                {
                    LockSupport.parkNanos(nanos);
                }
                nanos -= System.nanoTime() - t0;
                if (nanos < 0)
                {
                    cancel(self);
                    return null;
                }
                t0 = System.nanoTime();
            }
            if (Thread.currentThread().isInterrupted())
            {
                cancel(self);
                return null;
            }
            if (pred.status == CANCELED)
            {
                while (pred != h && (pred = pred.prev).status == CANCELED)
                    ;
            }
        } while (true);
    }
    
    public E take()
    {
        Node self = enqueue();
        Node pred = self.prev;
        Node h;
        do
        {
            if (pred == (h = head))
            {
                E result = get();
                if (result == null)
                {
                    LockSupport.park();
                }
                else
                {
                    head = self;
                    unparkSuccessor(self);
                    return result;
                }
            }
            else if (pred.status == CANCELED)
            {
                // 寻找到非取消节点的最靠近的head的节点作为新的前置节点
                while (pred != h && (pred = pred.prev).status == CANCELED)
                    ;
            }
            else
            {
                LockSupport.park();
            }
            if (Thread.currentThread().isInterrupted())
            {
                cancel(self);
                return null;
            }
        } while (true);
    }
    
    /**
     * 唤醒后续节点。
     * 
     * @param node
     */
    private void unparkSuccessor(Node node)
    {
        Thread nextWaiter = node.successor;
        if (node != tail)
        {
            if (nextWaiter == null)
            {
                while ((nextWaiter = node.successor) == null)
                    ;
            }
            LockSupport.unpark(nextWaiter);
        }
    }
    
    private void cancel(Node node)
    {
        node.status = CANCELED;
        Node pred = node.prev;
        // 防止前面的节点的唤醒浪费
        pred.successor = null;
        unparkSuccessor(node);
    }
    
}
