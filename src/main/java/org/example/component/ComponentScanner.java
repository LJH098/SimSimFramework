package org.example.component;

import org.example.annotation.Autowired;
import org.example.annotation.Component;
import org.example.annotation.Scope;
import org.example.bean.BeanContainer;
import org.example.bean.BeanDefinition;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

public class ComponentScanner {

    // IoC 컨테이너
    private final BeanContainer container;

    public ComponentScanner(BeanContainer container) {
        this.container = container;
    }

    public void scan(String basePackage) throws Exception {
        // basePackage의 .을 /로 변경
        String path = basePackage.replace('.', '/');
        // 클래스패스/basePackage
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        // null 검증
        if (resource == null) throw new RuntimeException("Invalid package: " + basePackage);
        // 클래스 경로 File객체로 변환
        File baseDir = new File(resource.toURI());
        // 디렉토리 내 모든 파일을 가져온다.
        scanDirectoryRecursive(baseDir, basePackage);

    }

    private void scanDirectoryRecursive(File dir, String currentPackage) throws Exception {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                // 하위 디렉토리는 패키지 이름을 갱신해서 재귀 호출
                scanDirectoryRecursive(file, currentPackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                // 클래스 명만 따로 파싱
                String className = currentPackage + "." + file.getName().replace(".class", "");
                // 문자열로 된 클래스 이름을 통해 JVM 에 클래스를 로드
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Component.class)) {
                    String scope = "singleton"; // Scope 기본값 싱글톤
                    // Scope 어노테이션이 있다면 해당 스코프를 저장한다.
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        scope = clazz.getAnnotation(Scope.class).value();
                    }
                    // Autowired 붙은 생성자를 우선적 조회 후 없으면 기본생성자를 조회한다.
                    Constructor<?> constructor = findAppropriateConstructor(clazz);
                    // 클래스 이름의 첫 글자만 소문자로 변경한다(ex. SimSimClass -> simSimClass)
                    String beanName = Introspector.decapitalize(clazz.getSimpleName());
                    // Bean 명세를 정의한다.
                    BeanDefinition def = new BeanDefinition(clazz, scope, beanName, constructor);
                    // 컨테이너에 해당 빈을 등록한다.
                    // TODO : 빈 등록순서를 정해야한다. 의존성에 따른 순서가 보장되어야함.
                    container.registerBeanDefinition(def);
                }
            }
        }
    }

    // 클래스의 모든 생성자를 조회 후, Autowired 가 붙은 생성자가 있는 경우 해당 생성자 사용. 없는 경우 기본생성자를 반환한다.
    private Constructor<?> findAppropriateConstructor(Class<?> clazz) throws NoSuchMethodException {
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(Autowired.class)) {
                return ctor;
            }
        }
        // 기본 생성자 fallback
        return clazz.getDeclaredConstructor();
    }
}

