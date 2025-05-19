package org.example.test;

import org.example.component.Component;

@Component
public class TestService {
    public void sayHello() {
        System.out.println("Hello from @Component!");
    }
}
