package org.example.test;

import org.example.annotation.Autowired;
import org.example.annotation.Component;
import org.example.annotation.PostConstruct;
import org.example.annotation.PreDestroy;

//@Component
public class TestService {
    @Autowired
    private TestServiceB testServiceB;
    @PostConstruct
    public void sayHello() {
        System.out.println("Hello from @Component!");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("shutting down!");
    }
}
