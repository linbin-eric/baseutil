package cc.jfire.baseutil;

import cc.jfire.baseutil.uniqueid.AutumnId;
import cc.jfire.baseutil.uniqueid.SummerId;
import cc.jfire.baseutil.uniqueid.Uid;
import org.junit.Test;

public class IdTest
{

    @Test
    public void test2()
    {
        Uid uid = AutumnId.instance();
        for (int i = 0; i < 10; i++)
        {
            System.out.println(uid.generateDigits());
        }
    }

    @Test
    public void test34()
    {
        Uid uid = new SummerId(1);
        for (int i = 0; i < 10; i++)
        {
            System.out.println(uid.generateLong());
        }
        System.out.println(Long.MAX_VALUE);
    }
}
