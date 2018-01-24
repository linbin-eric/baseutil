package com.jfireframework.baseutil;

import org.junit.Assert;
import org.junit.Test;
import com.jfireframework.baseutil.reflect.copy.Copy;
import com.jfireframework.baseutil.reflect.copy.CopyFrom;
import com.jfireframework.baseutil.reflect.copy.CopyTo;
import com.jfireframework.baseutil.reflect.copy.FastCopy;

public class CopyTest
{
	@Test
	public void test()
	{
		Copy<Person, Person> util = new FastCopy<CopyTest.Person, CopyTest.Person>() {};
		Person src = new Person();
		src.setAge(13);
		src.setSex(true);
		Person result = util.copy(src, new Person());
		Assert.assertTrue(result.getSex());
		Assert.assertEquals(13, result.getAge());
		Assert.assertFalse(result.isSex1());
	}
	
	@Test
	public void test_2()
	{
		Copy<Person, Person2> copy = new FastCopy<CopyTest.Person, CopyTest.Person2>() {};
		Person src = new Person();
		src.setAge(13);
		src.setSex(true);
		Person2 result = new Person2();
		Assert.assertNull(result.getSex2());
		copy.copy(src, result);
		Assert.assertTrue(result.getSex2());
	}
	
	@Test
	public void test_3()
	{
		Copy<Person, Person> util = new FastCopy<CopyTest.Person, CopyTest.Person>() {};
		Person src = new Person();
		src.setAge(13);
		src.setSex(true);
		Person result = util.copy(src, new Person());
		Assert.assertTrue(result.getSex());
		Assert.assertEquals(13, result.getAge());
		Assert.assertFalse(result.isSex1());
	}
	
	@Test
	public void test_4()
	{
		Copy<Person, Person2> copy = new FastCopy<CopyTest.Person, CopyTest.Person2>() {};
		Person src = new Person();
		src.setAge(13);
		src.setSex(true);
		Person2 result = new Person2();
		Assert.assertNull(result.getSex2());
		copy.copy(src, result);
		Assert.assertTrue(result.getSex2());
	}
	
	public static class Person2
	{
		@CopyFrom(name = "sex", from = Person.class)
		private Boolean sex2;
		
		public Boolean getSex2()
		{
			return sex2;
		}
		
		public void setSex2(Boolean sex2)
		{
			this.sex2 = sex2;
		}
		
	}
	
	public static class Person
	{
		private String	name;
		private int		age;
		@CopyTo(name = "sex2", to = Person2.class)
		private Boolean	sex;
		private boolean	sex1;
		
		public boolean isSex1()
		{
			return sex1;
		}
		
		public void setSex1(boolean sex1)
		{
			this.sex1 = sex1;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
		
		public int getAge()
		{
			return age;
		}
		
		public void setAge(int age)
		{
			this.age = age;
		}
		
		public Boolean getSex()
		{
			return sex;
		}
		
		public void setSex(Boolean sex)
		{
			this.sex = sex;
		}
		
	}
	
}
