package cc.jfire.baseutil.yaml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * 哪一个不是默认值使用哪一个。
 * 字段上的注解会覆盖掉类上的注解
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YamlRename
{
    String value() default "";

    Class<? extends YamlRename.YamlRenameProcessor> processor() default YamlRenameProcessor.class;

    interface YamlRenameProcessor
    {
        YamlRenameProcessor DEFAULT = new YamlRenameProcessor()
        {
        };

        default String rename(Field field)
        {
            return field.getName();
        }
    }
}
