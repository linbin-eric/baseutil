package cc.jfire.baseutil.concurrent;

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

    public boolean add(T o)
    {
        long limit      = this.readIndex + capacity;
        long writeIndex = this.writeIndex;
        while (writeIndex < limit || writeIndex < (limit = this.readIndex + capacity))
        {
            long witnesses = UNSAFE.compareAndExchangeLong(this, WRITE_INDEX_OFFSET, writeIndex, writeIndex + 1);
            if (witnesses == writeIndex)
            {
                array[(int) (writeIndex & index)] = o;
                long round = writeIndex >> shift;
                UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), round);
                return true;
            }
            else
            {
                writeIndex = witnesses;
            }
        }
        return false;
    }

    public T poll()
    {
        long writeIndex = this.writeIndex;
        long readIndex  = this.readIndex;
        while (readIndex < writeIndex || readIndex < (writeIndex = this.writeIndex))
        {
            if (UNSAFE.getLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((readIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT)) == readIndex >> shift)
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
                readIndex  = this.readIndex;
                writeIndex = this.writeIndex;
            }
        }
        return null;
    }

    @Override
    public void pushBusyWait(T t)
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
                    array[(int) (writeIndex & index)] = t;
                    long round = writeIndex >> shift;
                    UNSAFE.putLongVolatile(flag, ARRAY_LONG_BASE_OFFSET + ((writeIndex & index) << ARRAY_LONG_INDEX_SCALE_SHIFT), round);
                    return;
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
                Thread.yield();
            }
        } while (true);
    }

    @Override
    public boolean isEmpty()
    {
        return readIndex == writeIndex;
    }
}
