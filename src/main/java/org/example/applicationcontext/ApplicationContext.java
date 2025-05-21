package org.example.applicationcontext;

import org.example.annotation.ComponentScan;
import org.example.applicationcontext.bean.BeanContainer;
import org.example.applicationcontext.bean.BeanDefinition;
import org.example.component.ComponentScanner;

public class ApplicationContext {

    private final BeanContainer container = new BeanContainer();

    public ApplicationContext(Class<?> configClass) throws Exception {
        // 1. @ComponentScan 가져오기
        ComponentScan scan = configClass.getAnnotation(ComponentScan.class);
        if (scan == null) {
            throw new RuntimeException("@ComponentScan이 붙어있지 않습니다.");
        }

        // 2. 지정된 패키지마다 컴포넌트 스캔
        for (String basePackage : scan.basePackages()) {
            ComponentScanner scanner = new ComponentScanner(container);
            scanner.scan(basePackage);  // 컨테이너에 자동 등록됨
        }

        //  3. 모든 singleton bean을 미리 생성하여 초기화 (Eager Init)
        for (BeanDefinition def : container.getAllDefinitions()) {
            if (def.isSingleton()) {
                container.getBean(def.getName()); // createBean 호출됨
            }
        }
    }

    public <T> T getBean(Class<T> type) {
        return container.getBean(type);
    }

    public void close() {
        container.close();
    }
}