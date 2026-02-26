package com.demo;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 简化版 Spring Boot - 核心启动器
 */
public class MySpringApplication {

    public static void run(Class<?> primarySource, String[] args) {
        System.out.println("=== MySpringBoot Starting ===");
        
        // 1. 创建应用上下文
        MyApplicationContext context = new MyApplicationContext();
        
        // 2. 扫描 Bean
        System.out.println("Scanning components...");
        Set<Class<?>> components = scanComponents(primarySource);
        System.out.println("Found " + components.size() + " components");
        
        // 3. 注册 Bean
        System.out.println("Registering beans...");
        for (Class<?> cls : components) {
            try {
                Object bean = cls.getDeclaredConstructor().newInstance();
                context.registerBean(cls.getSimpleName(), cls, bean);
                System.out.println("  Registered: " + cls.getSimpleName());
            } catch (Exception e) {
                System.out.println("  Skip: " + cls.getSimpleName() + " - " + e.getMessage());
            }
        }
        
        // 4. 依赖注入
        System.out.println("Injecting dependencies...");
        context.injectDependencies();
        
        // 5. 初始化
        System.out.println("Initializing...");
        context.initialize();
        
        System.out.println("=== MySpringBoot Started ===");
    }

    /**
     * 扫描组件 (简化版：只扫当前包)
     */
    private static Set<Class<?>> scanComponents(Class<?> primarySource) {
        Set<Class<?>> components = new HashSet<>();
        
        // 获取启动类所在包
        String packageName = primarySource.getPackageName();
        System.out.println("Package: " + packageName);
        
        // 尝试加载同包下的类 (简化实现)
        String[] classNames = {
            "com.demo.ServiceA",
            "com.demo.ServiceB", 
            "com.demo.Controller"
        };
        
        for (String className : classNames) {
            try {
                Class<?> cls = Class.forName(className);
                if (cls.isAnnotationPresent(MyComponent.class)) {
                    components.add(cls);
                }
            } catch (ClassNotFoundException e) {
                // 类不存在，跳过
            }
        }
        
        return components;
    }
}

/**
 * 自定义组件注解
 */
@Retention(RetentionPolicy.RUNTIME)
@interface MyComponent {
}

/**
 * 注入注解
 */
@Retention(RetentionPolicy.RUNTIME)
@interface MyAutowired {
}

/**
 * 简化版应用上下文
 */
class MyApplicationContext {
    private Map<String, Object> beans = new HashMap<>();
    private Map<String, Class<?>> beanTypes = new HashMap<>();
    
    public void registerBean(String name, Class<?> type, Object instance) {
        beans.put(name, instance);
        beanTypes.put(name, type);
    }
    
    public void injectDependencies() {
        for (Object bean : beans.values()) {
            for (Field field : getAllFields(bean.getClass())) {
                if (field.isAnnotationPresent(MyAutowired.class)) {
                    try {
                        field.setAccessible(true);
                        Object dependency = beans.get(field.getType().getSimpleName());
                        if (dependency != null) {
                            field.set(bean, dependency);
                            System.out.println("  Injected: " + field.getName() + " -> " + dependency.getClass().getSimpleName());
                        }
                    } catch (Exception e) {
                        System.out.println("  Failed to inject: " + field.getName());
                    }
                }
            }
        }
    }
    
    public void initialize() {
        for (Object bean : beans.values()) {
            // 调用 @PostConstruct 方法 (简化)
            System.out.println("  Initialized: " + bean.getClass().getSimpleName());
        }
    }
    
    private List<Field> getAllFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        while (cls != null && cls != Object.class) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return fields;
    }
}
