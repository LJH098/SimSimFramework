package org.example.component;

import org.example.annotation.Component;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

public class ComponentScanner {

    private final Map<Class<?>, Object> container = new HashMap<>();

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
                    // 기본생성자 가져온다.
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    // 사용가능하게 한다.
                    constructor.setAccessible(true);
                    // 객체 생성
                    Object instance = constructor.newInstance();
                    // 컨테이너에 넣는다.
                    container.put(clazz, instance);
                }
            }
        }
    }

    public <T> T getBean(Class<T> type) {
        return type.cast(container.get(type));
    }

    public Collection<Object> getAllBeans() {
        return container.values();
    }
}

