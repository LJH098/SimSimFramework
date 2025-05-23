package org.example.applicationcontext;

import org.example.annotation.ComponentScan;
import org.example.applicationcontext.bean.BeanContainer;
import org.example.applicationcontext.bean.BeanDefinition;
import org.example.component.ComponentScanner;
import org.example.processor.BeanPostProcessor;

import java.util.*;

public class ApplicationContext {

    private final BeanContainer container = new BeanContainer();
    private final ComponentScanner scanner;

    /*
    TODO: 스프링 DI 컨테이너 구조 개선(이진혁)
    1. BeanPostProcessor 구현체 작성 및 createBean()에 적용
    2. BeanFactory 추상화 도입 및 ApplicationContext 분리
    3. BeanFactoryPostProcessor 인터페이스 정의 및 구현
    4. invokeBeanFactoryPostProcessors 로직 구현 및 적용 시점 분리
     */
    public ApplicationContext(String rootPackage) throws Exception {
        this.scanner = new ComponentScanner(container);

        // 1. 설정 클래스 스캔 및 등록
        List<Class<?>> configClasses = findConfigClasses(rootPackage);
        if (configClasses.isEmpty()) {
            throw new RuntimeException("@ComponentScan이 붙은 설정 클래스를 찾을 수 없습니다.");
        }

        // 2. 설정 클래스의 basePackage들을 스캔
        scanBasePackages(configClasses);

        // 3. BeanPostProcessor 우선 등록
        registerBeanPostProcessors();

        // 4. 나머지 singleton bean들을 Eager Init
        initializeSingletonBeans();
    }

    public <T> T getBean(Class<T> type) {
        return container.getBean(type);
    }

    public void close() {
        container.close();
    }

    // @ComponentScan 이 붙은 설정 클래스들을 찾아 반환
    private List<Class<?>> findConfigClasses(String rootPackage) throws Exception {
        return scanner.findAllComponentScanClasses(rootPackage);
    }

    // 설정 클래스들로부터 basePackages를 추출하여 스캔
    private void scanBasePackages(List<Class<?>> configClasses) throws Exception {
        Set<String> uniquePackages = new HashSet<>();
        for (Class<?> configClass : configClasses) {
            ComponentScan scan = configClass.getAnnotation(ComponentScan.class);
            uniquePackages.addAll(Arrays.asList(scan.basePackages()));
        }

        for (String basePackage : uniquePackages) {
            scanner.scan(basePackage);
        }
    }

    // BeanPostProcessor 타입의 빈들을 먼저 인스턴스화하고 등록
    private void registerBeanPostProcessors() {
        for (BeanDefinition def : container.getAllDefinitions()) {
            if (BeanPostProcessor.class.isAssignableFrom(def.getType())) {
                Object processor = container.getBean(def.getName());
                container.registerPostProcessor((BeanPostProcessor) processor);
            }
        }
    }

    // 일반 singleton 빈들을 생성
    private void initializeSingletonBeans() {
        for (BeanDefinition def : container.getAllDefinitions()) {
            if (def.isSingleton() && !BeanPostProcessor.class.isAssignableFrom(def.getType())) {
                container.getBean(def.getName());
            }
        }
    }
}
