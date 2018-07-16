package com.jfireframework.baseutil;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.jfireframework.baseutil.concurrent.FastMPSCArrayQueue;
import com.jfireframework.baseutil.concurrent.MPSCArrayQueue;

@RunWith(Parameterized.class)
public class ArrayQueueRightTest
{
	private int				thraedNum	= 10;
	private int				send		= 1000000;
	private Integer[]		content		= new Integer[send];
	private int[]			result		= new int[send];
	private Queue<Integer>	queue;
	
	public ArrayQueueRightTest(Queue<Integer> queue)
	{
		for (int i = 0; i < send; i++)
		{
			content[i] = i;
		}
		Arrays.fill(result, -1);
		this.queue = queue;
	}
	
	@Parameters
	public static Collection<?> params()
	{
		return Arrays.asList(new Object[][] { { new MPSCArrayQueue<Integer>(512) }, //
		        { new FastMPSCArrayQueue<Integer>(512) } });
	}
	
	@Test
	public void test() throws InterruptedException
	{
		final AtomicInteger index = new AtomicInteger(0);
		final CyclicBarrier barrier = new CyclicBarrier(thraedNum + 1);
		for (int i = 0; i < thraedNum; i++)
		{
			new Thread(new Runnable() {
				
				@Override
				public void run()
				{
					try
					{
						barrier.await();
					}
					catch (Exception e)
					{
						;
					}
					int j = -1;
					while ((j = index.getAndIncrement()) < send)
					{
						Integer integer = content[j];
						if (queue.offer(integer) == false)
						{
							while (queue.offer(integer) == false)
							{
								Thread.yield();
							}
						}
					}
				}
			}).start();
		}
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run()
			{
				try
				{
					barrier.await();
					int count = 0;
					int send = ArrayQueueRightTest.this.send;
					while (count < send)
					{
						Integer integer = queue.poll();
						// System.out.println("发现" + integer);
						if (integer == null)
						{
							while ((integer = queue.poll()) == null)
							{
								Thread.yield();
							}
						}
						result[integer] = integer;
						count++;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
			}
		});
		thread.start();
		thread.join();
		for (int i = 0; i < send; i++)
		{
			assertEquals("问题序号" + i, i, result[i]);
		}
	}
	
}
