package org.example.processor;

public interface BeanPostProcessor {
    /**
     * 빈이 초기화되기 전에 호출됨 (@PostConstruct 전에)
     */
    Object postProcessBeforeInitialization(Object bean, String beanName);

    /**
     * 빈이 초기화된 후 호출됨 (필드 주입 + PostConstruct 이후)
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}
