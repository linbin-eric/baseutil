package xin.nb1.baseutil.concurrent;

import xin.nb1.baseutil.NumberUtil;

import java.util.Arrays;

public class IndexReadCycleArray<T> extends L2Pad implements CycleArray<T>
{
    private final Object[] array;
    private final long[]   flag;
    private final int      index;
    private final int      capacity;

    public IndexReadCycleArray(int size)
    {
        capacity = NumberUtil.normalizeSize(size);
        index    = capacity - 1;
        array    = new Object[capacity];
        flag     = new long[capacity];
        Arrays.fill(array, null);
        for (int i = 0; i < flag.length; i++)
        {
            flag[i] = i - index;
        }
    }

    @Override
    public boolean add(T t)
    {
        long limit      = readIndex + capacity;
        long writeIndex = this.writeIndex;
        while (writeIndex < limit || writeIndex < (limit = readIndex + capacity))
        {
            int  position = (int) (writeIndex & index);
            long witness  = UNSAFE.compareAndExchangeLong(this, WRITE_INDEX_OFFSET, writeIndex, writeIndex + 1);
            if (witness == writeIndex)
            {
                long allow   = writeIndex - index;
                long l_index = ARRAY_LONG_BASE_OFFSET + (position << ARRAY_LONG_INDEX_SCALE_SHIFT);
                if (UNSAFE.getLongVolatile(flag, l_index) != allow)
                {
                    while (UNSAFE.getLongVolatile(flag, l_index) != allow)
                    {
                        ;
                    }
                }
                array[position] = t;
                UNSAFE.putLongVolatile(flag, l_index, writeIndex);
                return true;
            }
            else
            {
                writeIndex = witness;
            }
        }
        return false;
    }

    @Override
    public T poll()
    {
        long readIndex = this.readIndex;
        long limit     = writeIndex;
        do
        {
            if (readIndex < limit || readIndex < (limit = writeIndex))
            {
                long witness = UNSAFE.compareAndExchangeLong(this, READ_INDEX_OFFSET, readIndex, readIndex + 1);
                int  posi    = (int) (readIndex & index);
                if (witness == readIndex)
                {
                    long l_index = ARRAY_LONG_BASE_OFFSET + (posi << ARRAY_LONG_INDEX_SCALE_SHIFT);
                    long current = UNSAFE.getLongVolatile(flag, l_index);
                    if (current != readIndex)
                    {
                        while (UNSAFE.getLongVolatile(flag, l_index) != readIndex)
                        {
                            ;
                        }
                    }
                    Object result = array[posi];
                    UNSAFE.putLongVolatile(flag, l_index, readIndex + 1);
                    return (T) result;
                }
                else
                {
                    readIndex = witness;
                }
            }
            else
            {
                return null;
            }
        } while (true);
    }

    @Override
    public void pushBusyWait(T t)
    {
        long limit      = readIndex + capacity;
        long writeIndex = this.writeIndex;
        do
        {
            if (writeIndex < limit || writeIndex < (limit = readIndex + capacity))
            {
                long newWriteIndex = writeIndex + 1;
                long witness       = UNSAFE.compareAndExchangeLong(this, WRITE_INDEX_OFFSET, writeIndex, newWriteIndex);
                if (witness == writeIndex)
                {
                    long allow   = writeIndex - index;
                    long current = UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT));
                    if (current != allow)
                    {
                        while (UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT)) != allow)
                        {
                            Thread.yield();
                        }
                    }
                    array[(int) (writeIndex & index)] = t;
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), writeIndex);
                    return;
                }
                else
                {
                    writeIndex = witness;
                }
            }
            else
            {
                Thread.yield();
            }
        } while (true);
    }

    @Override
    public boolean isEmpty()
    {
        return writeIndex == readIndex;
    }
}
