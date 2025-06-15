package com.jfirer.baseutil.concurrent;

import com.jfirer.baseutil.reflect.ReflectUtil;
import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.Data;

import java.util.Arrays;

@Data
abstract class L1Pad
{
    byte b000, b001, b002, b003, b004, b005, b006, b007;//  8b
    byte b010, b011, b012, b013, b014, b015, b016, b017;// 16b
    byte b020, b021, b022, b023, b024, b025, b026, b027;// 24b
    byte b030, b031, b032, b033, b034, b035, b036, b037;// 32b
    byte b040, b041, b042, b043, b044, b045, b046, b047;// 40b
    byte b050, b051, b052, b053, b054, b055, b056, b057;// 48b
    byte b060, b061, b062, b063, b064, b065, b066, b067;// 56b
    byte b070, b071, b072, b073, b074, b075, b076, b077;// 64b
    byte b100, b101, b102, b103, b104, b105, b106, b107;// 72b
    byte b110, b111, b112, b113, b114, b115, b116, b117;// 80b
    byte b120, b121, b122, b123, b124, b125, b126, b127;// 88b
    byte b130, b131, b132, b133, b134, b135, b136, b137;// 96b
    byte b140, b141, b142, b143, b144, b145, b146, b147;//104b
    byte b150, b151, b152, b153, b154, b155, b156, b157;//112b
    byte b160, b161, b162, b163, b164, b165, b166, b167;//120b
    byte b170, b171, b172, b173, b174, b175, b176, b177;//128b

    protected static int getPowerOfTwo(long l)
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
}

abstract class L1Field extends L1Pad
{
    protected static final long   ARRAY_LONG_INDEX_SCALE_SHIFT = getPowerOfTwo(Unsafe.ARRAY_LONG_INDEX_SCALE);
    protected static final long   ARRAY_LONG_BASE_OFFSET       = Unsafe.ARRAY_LONG_BASE_OFFSET;
    protected static final Unsafe UNSAFE                       = ReflectUtil.UNSAFE;
    protected volatile     long   writeIndex;
}

@Data
abstract class L2Pad extends L1Field
{
    byte b000, b001, b002, b003, b004, b005, b006, b007;//  8b
    byte b010, b011, b012, b013, b014, b015, b016, b017;// 16b
    byte b020, b021, b022, b023, b024, b025, b026, b027;// 24b
    byte b030, b031, b032, b033, b034, b035, b036, b037;// 32b
    byte b040, b041, b042, b043, b044, b045, b046, b047;// 40b
    byte b050, b051, b052, b053, b054, b055, b056, b057;// 48b
    byte b060, b061, b062, b063, b064, b065, b066, b067;// 56b
    byte b070, b071, b072, b073, b074, b075, b076, b077;// 64b
    byte b100, b101, b102, b103, b104, b105, b106, b107;// 72b
    byte b110, b111, b112, b113, b114, b115, b116, b117;// 80b
    byte b120, b121, b122, b123, b124, b125, b126, b127;// 88b
    byte b130, b131, b132, b133, b134, b135, b136, b137;// 96b
    byte b140, b141, b142, b143, b144, b145, b146, b147;//104b
    byte b150, b151, b152, b153, b154, b155, b156, b157;//112b
    byte b160, b161, b162, b163, b164, b165, b166, b167;//120b
    byte b170, b171, b172, b173, b174, b175, b176, b177;//128b
    protected volatile     long readIndex;
    protected static final long WRITE_INDEX_OFFSET = UNSAFE.objectFieldOffset(L1Field.class, "writeIndex");
    protected static final long READ_INDEX_OFFSET  = UNSAFE.objectFieldOffset(L2Pad.class, "readIndex");
}

public class RelaxationReadCycleArray<T> extends L2Pad implements CycleArray<T>
{
    private final Object[] array;
    private final long[]   flag;
    private final int      shift;
    private final int      index;
    private final int      capacity;

    public RelaxationReadCycleArray(int size)
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
     * @param t
     * @return
     */
    public boolean put(T t)
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
                    array[(int) (writeIndex & index)] = t;
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

    public T take()
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
                    return (T) result;
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
