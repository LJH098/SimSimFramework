package org.example.test;

import org.example.annotation.Component;

@Component
public class TestService {
    public void sayHello() {
        System.out.println("Hello from @Component!");
    }
}
