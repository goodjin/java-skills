package com.demo;

import java.util.*;

/**
 * 示例 Service B - 依赖 ServiceA
 */
@MyComponent
public class ServiceB {
    
    @MyAutowired
    private ServiceA serviceA;
    
    public void test() {
        System.out.println("ServiceB testing...");
        if (serviceA != null) {
            System.out.println(serviceA.sayHello("World"));
        } else {
            System.out.println("ServiceA not injected!");
        }
    }
}
