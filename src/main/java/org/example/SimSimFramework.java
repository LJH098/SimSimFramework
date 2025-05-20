package org.example;

import org.example.bean.BeanContainer;
import org.example.bean.BeanDefinition;
import org.example.component.ComponentScanner;
import org.example.test.TestService;
import org.example.test.TestServiceB;

import java.lang.reflect.Constructor;

public class SimSimFramework {
    public static void main(String[] args) throws Exception{
        try{
            BeanContainer container = new BeanContainer();

            BeanDefinition defA = new BeanDefinition(TestService.class, "singleton", "testService", TestService.class.getConstructor());
            BeanDefinition defB = new BeanDefinition(TestServiceB.class, "singleton", "testServiceB", TestServiceB.class.getConstructor());

            container.registerBeanDefinition(defB);
            container.registerBeanDefinition(defA);

            TestService testService = container.getBean(TestService.class);
            container.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}