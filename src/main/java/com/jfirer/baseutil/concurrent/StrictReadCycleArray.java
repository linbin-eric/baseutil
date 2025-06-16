package com.jfirer.baseutil.concurrent;

import javax.xml.transform.Source;
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
        long limit      = this.readIndex + capacity;
        long writeIndex = this.writeIndex;
        do
        {
            if (writeIndex < limit)
            {
                long newWriteIndex = writeIndex + 1;
                long witnesses     = UNSAFE.compareAndExchangeLong(this, WRITE_INDEX_OFFSET, writeIndex, newWriteIndex);
                if (witnesses == writeIndex)
                {
                    array[(int) (writeIndex & index)] = o;
                    long round= writeIndex >>shift;
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), round);
//                    System.out.println("writeIndex:"+writeIndex+",readIndex:"+readIndex+",round:"+round);
                    return true;
                }
                else
                {
                    writeIndex = witnesses;
                }
            }
            else if (writeIndex < (limit = this.readIndex + capacity))
            {
                ;
            }
            else
            {
                return false;
            }
        } while (true);
    }

    public T take()
    {
        long count      = 0;
        long writeIndex = this.writeIndex;
        long readIndex  = this.readIndex;
        long round;
        long current = 0;
        do
        {
            round = readIndex >> shift;
            if (readIndex < writeIndex)
            {
                current = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT));
                if (current == round)
                {
                    Object result    = array[(int) (readIndex & index)];
                    long   witnesses = UNSAFE.compareAndExchangeLong(this, READ_INDEX_OFFSET, readIndex, readIndex + 1);
                    if (witnesses == readIndex)
                    {
                        return (T) result;
                    }
                    else
                    {
                        readIndex = witnesses;
                    }
                }
                else
                {
                    readIndex =this.readIndex;
                    writeIndex = this.writeIndex;
                }
            }
            else if (readIndex < (writeIndex = this.writeIndex))
            {
                ;
            }
            else
            {
                return null;
            }
        } while (++count < 200000000);
        System.err.println("异常,readIndex:" + readIndex + ",writeIndex:" + writeIndex+",round:"+round+",current:"+current+",real writeIndex:"+this.writeIndex);
        System.exit(0);
        return null;
    }
}
