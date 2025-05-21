package org.example;

import org.example.applicationcontext.ApplicationContext;
import org.example.config.AppConfig;
import org.example.test.TestService;

public class SimSimFramework {
    public static void main(String[] args) throws Exception{
        try{
            ApplicationContext context = new ApplicationContext(AppConfig.class);

            TestService testService = context.getBean(TestService.class);
            testService.doSomething();
            context.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}