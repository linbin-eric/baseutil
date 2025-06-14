package com.jfirer.baseutil.concurrent;

import com.jfirer.baseutil.reflect.ReflectUtil;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.util.Arrays;

public class CycleArray
{
    private static final int      ELEMENT_EMPTY            = -1;
    private static final int      ELEMENT_HAS              = 0;
    private static final long     ARRAY_OBJECT_BASE_OFFSET = Unsafe.ARRAY_OBJECT_BASE_OFFSET;
    private static final long     ARRAY_OBJECT_INDEX_SCALE = Unsafe.ARRAY_OBJECT_INDEX_SCALE;
    private static final long     ARRAY_INT_INDEX_SCALE    = Unsafe.ARRAY_INT_INDEX_SCALE;
    private static final long     ARRAY_INT_BASE_OFFSET    = Unsafe.ARRAY_INT_BASE_OFFSET;
    private static final Unsafe   UNSAFE                   = ReflectUtil.UNSAFE;
    private static final long     WRITE_INDEX_OFFSET       = UNSAFE.objectFieldOffset(CycleArray.class, "writeIndex");
    private static final long     READ_INDEX_OFFSET        = UNSAFE.objectFieldOffset(CycleArray.class, "readIndex");
    private final        Object[] array;
    private final        int[]    flag;
    private final        int      shift;
    private final        int      index;
    private final        int      capacity;
    private volatile     long     writeIndex;
    private volatile     long     readIndex;

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
        index    = count;
        shift    = realSize - 1;
        array    = new Object[realSize];
        flag     = new int[realSize];
        Arrays.fill(array, null);
        Arrays.fill(flag, ELEMENT_EMPTY);
    }

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
                    if (UNSAFE.getIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (writeIndex & shift) * ARRAY_INT_INDEX_SCALE) == ELEMENT_EMPTY)
                    {
                        array[(int) (writeIndex & shift)] = o;
                        UNSAFE.putIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (writeIndex & shift) * ARRAY_INT_INDEX_SCALE, ELEMENT_HAS);
                    }
                    else
                    {
                        while (UNSAFE.getIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (writeIndex & shift) * ARRAY_INT_INDEX_SCALE) == ELEMENT_HAS)
                        {
                            ;
                        }
                        array[(int) (writeIndex & shift)] = o;
                        UNSAFE.putIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (writeIndex & shift) * ARRAY_INT_INDEX_SCALE, ELEMENT_HAS);
                    }
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
                    if (UNSAFE.getIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (readIndex & shift) * ARRAY_INT_INDEX_SCALE) == ELEMENT_HAS)
                    {
                        Object result = array[(int) (readIndex & shift)];
                        UNSAFE.putIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (readIndex & shift) * ARRAY_INT_INDEX_SCALE, ELEMENT_EMPTY);
                        return result;
                    }
                    else
                    {
                        while (UNSAFE.getIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (readIndex & shift) * ARRAY_INT_INDEX_SCALE) == ELEMENT_EMPTY)
                        {
                            ;
                        }
                        Object result = array[(int) (readIndex & shift)];
                        UNSAFE.putIntVolatile(flag, ARRAY_INT_BASE_OFFSET + (readIndex & shift) * ARRAY_INT_INDEX_SCALE, ELEMENT_EMPTY);
                        return result;
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
        } while (true);
    }
}
