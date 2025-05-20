package org.example.bean;

import java.lang.reflect.Constructor;
/**
 * Class<?> type : 실제 클래스 타입
 * String scope : "singleton" or "prototype"
 * String name : 빈 이름 (기본은 클래스 simpleName 기준)
 * Constructor<?> constructor : 의존성 주입에 사용할 생성자
 * boolean isPrimary : (선택) 동일 타입 중 우선 등록 여부
 * */
public class BeanDefinition {
    private final Class<?> type;
    private final String scope;
    private final String name;
    private final Constructor<?> constructor;

    public BeanDefinition(Class<?> type, String scope, String name, Constructor<?> constructor) {
        this.type = type;
        this.scope = scope;
        this.name = name;
        this.constructor = constructor;
    }

    public Class<?> getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public boolean isSingleton() {
        return "singleton".equalsIgnoreCase(scope);
    }

    public boolean isPrototype() {
        return "prototype".equalsIgnoreCase(scope);
    }
}

