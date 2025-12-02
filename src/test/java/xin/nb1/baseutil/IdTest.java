package xin.nb1.baseutil;

import xin.nb1.baseutil.uniqueid.AutumnId;
import xin.nb1.baseutil.uniqueid.SummerId;
import xin.nb1.baseutil.uniqueid.Uid;
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
