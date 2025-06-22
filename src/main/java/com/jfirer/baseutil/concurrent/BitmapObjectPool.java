package com.jfirer.baseutil.concurrent;

import java.util.SplittableRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BitmapObjectPool<T>
{
    private static final SplittableRandom     RANDOM                = new SplittableRandom();
    private static final int                  MAX_SEGMENTS_TO_CHECK = 64; // Restrict traversal range
    private final        Object[][]           objectsSegments;
    private final        long[][]             bitmapSegments;
    private final        ReentrantLock[]      locks;
    private final        int[]                availableCounts;
    private final        int                  actualCapacity;
    private final        int                  segmentSize;
    private final        int                  segmentCount;
    private final        int                  segmentSizeShift; // log2(segmentSize)
    private final        Function<Integer, T> function;
    private volatile     int                  lastSuccessfulSegment;

    @SuppressWarnings("unchecked")
    public BitmapObjectPool(Function<Integer, T> function, int requestedCapacity)
    {
        if (function == null)
        {
            throw new IllegalArgumentException("Function cannot be null");
        }
        if (requestedCapacity <= 0)
        {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.function = function;
        // Calculate segmentSize as a power of 2 and multiple of 64
        int cpuCount         = Runtime.getRuntime().availableProcessors();
        int idealSegmentSize = Math.max(64, (requestedCapacity + cpuCount - 1) / cpuCount);
        // Find the next power of 2, then adjust to nearest multiple of 64
        int powerOfTwo = Integer.highestOneBit(idealSegmentSize - 1) << 1;
        this.segmentSize      = Math.max(((powerOfTwo + 63) / 64) * 64, 64); // Round up to multiple of 64
        this.segmentSizeShift = Integer.numberOfTrailingZeros(segmentSize);
        // Calculate segmentCount as a power of 2
        int minSegmentCount = (requestedCapacity + segmentSize - 1) / segmentSize;
        this.segmentCount = Math.max((Integer.highestOneBit(minSegmentCount - 1) << 1), 1);
        // Calculate actual capacity
        this.actualCapacity        = segmentSize * segmentCount;
        this.objectsSegments       = new Object[segmentCount][];
        this.bitmapSegments        = new long[segmentCount][];
        this.locks                 = new ReentrantLock[segmentCount];
        this.availableCounts       = new int[segmentCount];
        this.lastSuccessfulSegment = 0;
        for (int i = 0; i < segmentCount; i++)
        {
            objectsSegments[i] = new Object[segmentSize];
            int bitmapSize = segmentSize >> 6; // Exact division, since segmentSize is multiple of 64
            bitmapSegments[i]  = new long[bitmapSize];
            locks[i]           = new ReentrantLock();
            availableCounts[i] = segmentSize; // All segments have full segmentSize available
//            for (int j = 0; j < segmentSize; j++)
//            {
//                int index  = (i << segmentSizeShift) | j; // Bit operation: i * segmentSize + j
//                T   object = function.apply(index);
//                if (object == null)
//                {
//                    throw new IllegalStateException("Function returned null object at index " + index);
//                }
//                objectsSegments[i][j] = object;
//            }
        }
    }

    @SuppressWarnings("unchecked")
    public T acquire()
    {
        // First phase: Try lastSuccessfulSegment with tryLock
        int startSegment = lastSuccessfulSegment;
        if (locks[startSegment].tryLock())
        {
            try
            {
                T result = tryAcquireFromSegment(startSegment);
                if (result != null)
                {
                    lastSuccessfulSegment = startSegment;
                    return result;
                }
            }
            finally
            {
                locks[startSegment].unlock();
            }
        }
        // Second phase: Random start with restricted traversal using tryLock
        int maxSegmentsToCheck = Math.min(segmentCount, MAX_SEGMENTS_TO_CHECK);
        int randomStart        = RANDOM.nextInt(segmentCount);
        for (int i = 0; i < maxSegmentsToCheck; i++)
        {
            int segmentIndex = (randomStart + i) % segmentCount;
            if (segmentIndex != startSegment && locks[segmentIndex].tryLock())
            {
                try
                {
                    T result = tryAcquireFromSegment(segmentIndex);
                    if (result != null)
                    {
                        lastSuccessfulSegment = segmentIndex;
                        return result;
                    }
                }
                finally
                {
                    locks[segmentIndex].unlock();
                }
            }
        }
        // Third phase: Random start with restricted traversal using lock
        randomStart = RANDOM.nextInt(segmentCount);
        for (int i = 0; i < maxSegmentsToCheck; i++)
        {
            int segmentIndex = (randomStart + i) % segmentCount;
            locks[segmentIndex].lock();
            try
            {
                T result = tryAcquireFromSegment(segmentIndex);
                if (result != null)
                {
                    lastSuccessfulSegment = segmentIndex;
                    return result;
                }
            }
            finally
            {
                locks[segmentIndex].unlock();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private T tryAcquireFromSegment(int segmentIndex)
    {
        // Lock already acquired by caller
        if (availableCounts[segmentIndex] == 0) return null;
        long[] bitmap = bitmapSegments[segmentIndex];
        for (int j = 0; j < bitmap.length; j++)
        {
            if (bitmap[j] != ~0L)
            {
                int bitIndex = Long.numberOfTrailingZeros(~bitmap[j]);
                if (bitIndex < 64)
                {
                    int localIndex = (j << 6) | bitIndex; // Bit operation: j * 64 + bitIndex
                    bitmap[j] |= (1L << bitIndex);
                    availableCounts[segmentIndex]--;
                    T t = (T) objectsSegments[segmentIndex][localIndex];
                    if (t == null)
                    {
                        int index = (segmentIndex << segmentSizeShift) | localIndex; // Bit operation: i * segmentSize + j
                        t                                         = function.apply(index);
                        objectsSegments[segmentIndex][localIndex] = t;
                    }
                    return t;
                }
            }
        }
        availableCounts[segmentIndex] = 0;
        return null;
    }

    public boolean release(int index)
    {
        if (index < 0 || index >= actualCapacity)
        {
            return false;
        }
        int           segmentIndex = index >> segmentSizeShift; // Bit operation: index / segmentSize
        int           localIndex   = index & (segmentSize - 1);   // Bit operation: index % segmentSize
        int           bitmapIndex  = localIndex >> 6;           // Bit operation: localIndex / 64
        int           bitIndex     = localIndex & 63;              // Bit operation: localIndex % 64
        ReentrantLock lock         = locks[segmentIndex];
        lock.lock();
        try
        {
            long[] bitmap = bitmapSegments[segmentIndex];
            if ((bitmap[bitmapIndex] & (1L << bitIndex)) == 0)
            {
                return false;
            }
            bitmap[bitmapIndex] &= ~(1L << bitIndex);
            availableCounts[segmentIndex]++;
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    public int getCapacity()
    {
        return actualCapacity; // Return actual capacity
    }

    public int getAvailableCount()
    {
        int count = 0;
        for (int i = 0; i < segmentCount; i++)
        {
            ReentrantLock lock = locks[i];
            lock.lock();
            try
            {
                count += availableCounts[i];
            }
            finally
            {
                lock.unlock();
            }
        }
        return count;
    }
}