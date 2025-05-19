package org.example;

import org.example.component.ComponentScanner;
import org.example.test.TestService;

public class Main {
    public static void main(String[] args) throws Exception{
        try{
            ComponentScanner scanner = new ComponentScanner();
            scanner.scan("org.example"); // 스캔할 패키지

            TestService service = scanner.getBean(TestService.class);
            service.sayHello();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}