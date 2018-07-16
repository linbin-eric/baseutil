package com.jfireframework.baseutil;

import java.util.Queue;
import java.util.concurrent.CyclicBarrier;
import org.jctools.queues.MpscArrayQueue;
import org.junit.Test;
import com.jfireframework.baseutil.concurrent.FastMPSCArrayQueue;
import com.jfireframework.baseutil.time.Timewatch;

public class ArrayQueueSpeedTest
{
	int				capacity			= 1024;
	int				producerThreadNum	= 12;
	final int		sendNum				= 1000000;
	final int		total				= sendNum * producerThreadNum;
	final String	value				= "";
	
	public void testJcToolArray() throws InterruptedException
	{
		final MpscArrayQueue<Object> jcToolArray = new MpscArrayQueue<Object>(capacity);
		for (int i = 0; i < 1000; i++)
		{
			jcToolArray.offer(value);
			jcToolArray.poll();
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
						if (jcToolArray.offer(value) == false)
						{
							while (jcToolArray.offer(value) == false)
							{
								// LockSupport.parkNanos(1);
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
					if (jcToolArray.poll() == null)
					{
						while (jcToolArray.poll() == null)
						{
							// LockSupport.parkNanos(1);
							Thread.yield();
						}
					}
				}
				timewatch.end();
				System.out.println("消费" + total / 10000 + "w个数据耗时:" + timewatch.getTotal() + "毫秒");
				if (jcToolArray.isEmpty() == false)
				{
					System.err.println("异常");
				}
			}
		});
		thread.start();
		thread.join();
	}
	
	public void testBastUtilArrayQueue() throws InterruptedException
	{
		final Queue<Object> baseUtilArrayQueue = new FastMPSCArrayQueue<Object>(capacity);
		for (int i = 0; i < 10000; i++)
		{
			baseUtilArrayQueue.offer(value);
			baseUtilArrayQueue.poll();
			Thread.yield();
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
						String value = String.valueOf(i);
						if (baseUtilArrayQueue.offer(value) == false)
						{
							while (baseUtilArrayQueue.offer(value) == false)
							{
								// LockSupport.parkNanos(1);
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
					if (baseUtilArrayQueue.poll() == null)
					{
						while (baseUtilArrayQueue.poll() == null)
						{
							// LockSupport.parkNanos(1);
							Thread.yield();
						}
					}
				}
				timewatch.end();
				System.out.println("消费" + total / 10000 + "w个数据耗时:" + timewatch.getTotal() + "毫秒");
				if (baseUtilArrayQueue.isEmpty() == false)
				{
					System.err.println("异常");
				}
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void test() throws InterruptedException
	{
		testBastUtilArrayQueue();
	}
}
