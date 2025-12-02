package xin.nb1.baseutil.smc.compiler.test;

import xin.nb1.baseutil.reflect.valueaccessor.ValueAccessor;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.DefaultLoggingEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Data
public class HelloComponent
{
    private              String name   = "222";

    @SneakyThrows
    @PostConstruct
    public void init()
    {
        log.info("hello world");
        ValueAccessor valueAccessor = ValueAccessor.compile(HelloComponent.class.getDeclaredField("name"));
        ValueAccessor.compile(DefaultLoggingEvent.class.getDeclaredField("message"));
        log.info("name:{}", valueAccessor.getReference(this));
    }
}