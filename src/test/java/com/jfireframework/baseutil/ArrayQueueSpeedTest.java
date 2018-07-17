package com.jfireframework.baseutil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.MpscLinkedQueue7;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.jfireframework.baseutil.concurrent.FastMPSCArrayQueue;
import com.jfireframework.baseutil.concurrent.MPSCArrayQueue;
import com.jfireframework.baseutil.concurrent.MPSCLinkedQueue;
import com.jfireframework.baseutil.time.Timewatch;

@RunWith(Parameterized.class)
public class ArrayQueueSpeedTest
{
	int						producerThreadNum	= 12;
	final int				sendNum				= 1000000;
	final int				total				= sendNum * producerThreadNum;
	final String			value				= "";
	private Queue<Object>	queue;
	
	public ArrayQueueSpeedTest(Queue<Object> queue)
	{
		this.queue = queue;
	}
	
	@Parameters
	public static Collection<Object[]> param()
	{
		return Arrays.asList(new Object[][] { //
		        { new MpscLinkedQueue7<Object>() }, //
		        { new MPSCLinkedQueue<Object>() }, //
		        { new MPSCArrayQueue<Object>(1024) }, //
		        { new FastMPSCArrayQueue<Object>(1024) }, //
		        { new MpscArrayQueue<Object>(1024) }, //
		
		});
	}
	
	@Test
	public void testBastUtilArrayQueue() throws InterruptedException
	{
		for (int i = 0; i < 10000; i++)
		{
			queue.offer(value);
			queue.poll();
		}
		final CyclicBarrier barrier = new CyclicBarrier(producerThreadNum + 1);
		for (int i = 0; i < producerThreadNum; i++)
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
						e.printStackTrace();
					}
					for (int i = 0; i < sendNum; i++)
					{
						if (queue.offer(value) == false)
						{
							while (queue.offer(value) == false)
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
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				Timewatch timewatch = new Timewatch();
				timewatch.start();
				for (int i = 0; i < total; i++)
				{
					if (queue.poll() == null)
					{
						while (queue.poll() == null)
						{
							Thread.yield();
						}
					}
				}
				timewatch.end();
				System.out.println("消费" + total / 10000 + "w个数据耗时:" + timewatch.getTotal() + "毫秒");
				if (queue.isEmpty() == false)
				{
					System.err.println("异常");
				}
			}
		});
		thread.start();
		thread.join();
	}
	
}
