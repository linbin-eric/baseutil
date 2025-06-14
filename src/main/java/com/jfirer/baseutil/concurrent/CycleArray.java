package com.jfirer.baseutil.concurrent;

import com.jfirer.baseutil.reflect.ReflectUtil;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.util.Arrays;

public class CycleArray
{
    private static final long     ARRAY_OBJECT_BASE_OFFSET       = Unsafe.ARRAY_OBJECT_BASE_OFFSET;
    private static final long     ARRAY_OBJECT_INDEX_SCALE_SHIFT = getPowerOfTwo(Unsafe.ARRAY_OBJECT_INDEX_SCALE);
    private static final long     ARRAY_LONG_INDEX_SCALE_SHIFT   = getPowerOfTwo(Unsafe.ARRAY_LONG_INDEX_SCALE);
    private static final long     ARRAY_LONG_BASE_OFFSET         = Unsafe.ARRAY_LONG_BASE_OFFSET;
    private static final Unsafe   UNSAFE                         = ReflectUtil.UNSAFE;
    private static final long     WRITE_INDEX_OFFSET             = UNSAFE.objectFieldOffset(CycleArray.class, "writeIndex");
    private static final long     READ_INDEX_OFFSET              = UNSAFE.objectFieldOffset(CycleArray.class, "readIndex");
    private final        Object[] array;
    private final        long[]   flag;
    private final        int      shift;
    private final        int      index;
    private final        int      capacity;
    private volatile     long     writeIndex;
    private volatile     long     readIndex;

    private static int getPowerOfTwo(long l)
    {
        int  count = 0;
        long i     = 1;
        while (i < l)
        {
            i <<= 1;
            count++;
        }
        return count;
    }

    public CycleArray(int size)
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
        Arrays.fill(flag, 0);
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
    public boolean put(Object o)
    {
        do
        {
            long writeIndex = this.writeIndex;
            if (writeIndex < readIndex + capacity)
            {
                long newWriteIndex = writeIndex + 1;
                if (UNSAFE.compareAndSetLong(this, WRITE_INDEX_OFFSET, writeIndex, newWriteIndex))
                {
                    long current    = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT));
                    long legalRound = writeIndex >> shift;
                    if (current >> 1 != legalRound)
                    {
                        while ((current = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT))) >> 1 != legalRound)
                        {
                            ;
                        }
                    }
                    array[(int) (writeIndex & index)] = o;
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), current | 1L);
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

    public Object take()
    {
        do
        {
            long readIndex = this.readIndex;
            if (readIndex < writeIndex)
            {
                long newReadIndex = readIndex + 1;
                if (UNSAFE.compareAndSetLong(this, READ_INDEX_OFFSET, readIndex, newReadIndex))
                {
                    long current    = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT));
                    long legalRound = readIndex >> shift;
                    if (current >> 1 != legalRound)
                    {
                        while ((current = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT))) >> 1 != legalRound)
                        {
                            ;
                        }
                    }
                    int label = (int) (current & 0x01L);
                    if (label != 1)
                    {
                        while ((UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT)) & 0x01L) != 1)
                        {
                            ;
                        }
                    }
                    Object result = array[(int) (readIndex & index)];
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), ((legalRound + 1) << 1));
                    return result;
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
        } while (true);
    }
}
