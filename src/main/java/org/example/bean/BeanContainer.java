package org.example.bean;

import org.example.annotation.Autowired;
import org.example.annotation.PostConstruct;
import org.example.annotation.PreDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanContainer {

    // Bean 정의 저장소(동시성 제어)
    private final Map<String, BeanDefinition> definitionMap = new ConcurrentHashMap<>();

    // 싱글톤 인스턴스 저장소
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    // 타입 기반 빠른 조회용: 타입 -> 빈 이름들(Qualifier어노테이션 사용하기 위한 List타입. Map으로 변경할까..?)
    private final Map<Class<?>, List<String>> typeIndex = new ConcurrentHashMap<>();

    public void registerBeanDefinition(BeanDefinition def) {
        // 빈 이름 추출
        String name = def.getName();

        // BeanDefinition 등록
        definitionMap.put(name, def);

        // 타입 기반 인덱싱
        // Key 가 존재할 경우: 아무런 작업을 하지 않고 기존에 존재하는 Key의 Value를 리턴한다.
        // Key 가 존재하지 않는 경우: 람다식을 적용한 값을 해당 key에 저장한 후 newValue를 반환한다
        typeIndex.computeIfAbsent(def.getType(), k -> new ArrayList<>()).add(name);
    }

    // 빈 이름 기반 조회 및 생성
    public Object getBean(String name) {
        BeanDefinition def = definitionMap.get(name);
        if (def == null) throw new RuntimeException("No such bean: " + name);

        if (def.isSingleton()) {
            return singletonObjects.computeIfAbsent(name, n -> createBean(def));
        } else {
            return createBean(def); // prototype은 매번 새로 생성
        }
    }

    // 타입 기반 조회 (단일 매칭만 허용)
    public <T> T getBean(Class<T> type) {
        List<String> names = typeIndex.get(type);
        if (names == null || names.isEmpty()) { // 조회 결과 없는 경우
            throw new RuntimeException("No bean of type: " + type.getName());
        } else if (names.size() > 1) { // 조회된 빈이 다수인 경우.
            throw new RuntimeException("Multiple beans found for type: " + type.getName());
        }
        return type.cast(getBean(names.get(0)));
    }

    // Bean 인스턴스 생성
    private Object createBean(BeanDefinition def) {
        try {
            // 생성할 빈 내부에 Autowired 어노테이션이 붙어있는 생성자 조회
            Constructor<?> constructor = findAutowiredConstructor(def.getType());
            Object instance;

            // 생성자 조회 성공
            if (constructor != null) {
                // DI할 빈의 생성자 파라미터 타입 조회 후, 해당 타입에 맞는 빈을 컨테이너에서 조회한 후 Array로 만든다.
                Object[] args = Arrays.stream(constructor.getParameterTypes())
                        .map(this::getBean)
                        .toArray();
                // 생성자 사용가능 처리
                constructor.setAccessible(true);
                // 빈 인스턴스 생성
                instance = constructor.newInstance(args);
            } else { // 생성자 조회 실패
                // 기본 생성자 조회
                Constructor<?> defaultCtor = def.getConstructor();
                defaultCtor.setAccessible(true);
                instance = defaultCtor.newInstance();
            }

            // 2. 필드 주입
            injectFields(instance);

            // 3. 생명주기 콜백 (@PostConstruct)
            invokePostConstruct(instance);

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + def.getName(), e);
        }
    }

    // Autowired 어노테이션을 사용한 필드 객체의 생성자를 조회한다.
    private Constructor<?> findAutowiredConstructor(Class<?> clazz) {
        // 모든 생성자를 조회한다.
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            // 생성자에 Autowired 어노테이션이 존재하는 경우.
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;
            }
        }
        return null;
    }

    // 생성한 빈에 대한 필드를 주입한다
    private void injectFields(Object instance) throws IllegalAccessException {
        // 생성한 빈에 대한 필드들을 조회한다.
        for (Field field : instance.getClass().getDeclaredFields()) {
            // 필드에 Autowired 어노테이션이 붙어잇다면?
            if (field.isAnnotationPresent(Autowired.class)) {
                // 타입 가져온다
                Class<?> depType = field.getType();
                // 타입에 대한 빈 조회한다.
                Object dependency = getBean(depType);
                field.setAccessible(true);
                // 필드 주입
                field.set(instance, dependency);
            }
        }
    }
    // PostConstruct 어노테이션이 붙은 메서드를 선처리 해준다.
    private void invokePostConstruct(Object instance) throws Exception {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
    }

    // 생명주기 후처리 등을 위한 접근용 메서드
    public Collection<Object> getAllBeans() {
        return singletonObjects.values();
    }

    // 컨테이너 모든 값을 가져온다.
    public Collection<BeanDefinition> getAllDefinitions() {
        return definitionMap.values();
    }

    // 컨테이너 종료시 호출메서드
    public void close() {
        for (Object bean : singletonObjects.values()) {
            invokePreDestroy(bean);
        }
    }

    private void invokePreDestroy(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                try {
                    method.setAccessible(true);
                    // 해당 메서드 실행
                    method.invoke(instance);
                } catch (Exception e) {
                    System.err.println("Error during @PreDestroy: " + e.getMessage());
                }
            }
        }
    }


}

