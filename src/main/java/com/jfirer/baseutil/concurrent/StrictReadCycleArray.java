package com.jfirer.baseutil.concurrent;

import java.util.Arrays;

public class StrictReadCycleArray<T> extends L2Pad implements CycleArray<T>
{
    private final Object[] array;
    private final long[]   flag;
    private final int      shift;
    private final int      index;
    private final int      capacity;

    public StrictReadCycleArray(int size)
    {
        int realSize = 1;
        int count    = 0;
        while (realSize < size)
        {
            realSize = realSize << 1;
            count++;
        }
        capacity = realSize;
        shift    = count;
        index    = realSize - 1;
        array    = new Object[realSize];
        flag     = new long[realSize];
        Arrays.fill(array, null);
        Arrays.fill(flag, -1);
        readIndex = writeIndex = 0;
    }

    /**
     * flag右移一位的值有两个含义：
     * 1、代表当前可以写入的轮次。
     * 2、代表当前可以读取的轮次。
     * 写入的时候，轮次不变，最右位变为1；
     * 读取的时候轮次+1，最右位变为0。
     * <p>
     * 如果flag整个值只代表空或不空会导致并发异常，异常情况如下：
     * 1、并发写入，将writeIndex扩展到16（假设大小），并且执行完毕写入
     * 2、并发读取，CAS成功将readIndex扩展到16，但是还没有执行实际的读取工作。
     * 3、因为步骤2，并发写入，将writeIndex扩展到32，没有执行完毕写入。
     * 4、因为步骤3，并发读取，将readIndex扩展到32，此时会有两个读取线程持有不同的readIndex，但是对应的数组元素下标是同一个，造成读取到相同的数据。
     * <p>
     * 由于这个异常，因此需要引入轮次的概念，又由于需要区分读写，因此将最后一位用来表达空和非空的概念。
     *
     * @param o
     * @return
     */
    public boolean put(T o)
    {
        do
        {
            long writeIndex = this.writeIndex;
            if (writeIndex < readIndex + capacity)
            {
                long newWriteIndex = writeIndex + 1;
                if (UNSAFE.compareAndSetLong(this, WRITE_INDEX_OFFSET, writeIndex, newWriteIndex))
                {
                    array[(int) (writeIndex & index)] = o;
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), writeIndex >> shift);
                    return true;
                }
                else
                {
                    ;
                }
            }
            else
            {
                return false;
            }
        } while (true);
    }

    public T take()
    {
        long count = 0;
        do
        {
            long readIndex = this.readIndex;
            long round     = readIndex >> shift;
            if (readIndex < writeIndex)
            {
                long current = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT));
                if (current == round)
                {
                    Object result = array[(int) (readIndex & index)];
                    if (UNSAFE.compareAndSetLong(this, READ_INDEX_OFFSET, readIndex, readIndex + 1))
                    {
                        return (T) result;
                    }
                }
                else
                {
                    ;
                }
            }
            else
            {
                return null;
            }
        } while (count++<200000000);
        System.err.println("异常");
        System.exit(0);
        return  null;
    }
}
