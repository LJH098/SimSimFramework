package org.example;

import org.example.bean.BeanContainer;
import org.example.component.ComponentScanner;
import org.example.test.TestService;

public class SimSimFramework {
    public static void main(String[] args) throws Exception{
        try{
            BeanContainer container = new BeanContainer();
            ComponentScanner scanner = new ComponentScanner(container);
            scanner.scan("org.example"); // 패키지 전체 스캔

            TestService testService = container.getBean(TestService.class);
            testService.doSomething();
            container.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}