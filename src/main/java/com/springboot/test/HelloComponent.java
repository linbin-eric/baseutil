package com.springboot.test;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        log.info("name:{}", valueAccessor.getReference(this));
    }
}