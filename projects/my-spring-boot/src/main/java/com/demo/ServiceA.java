package com.demo;

/**
 * 示例 Service A
 */
@MyComponent
public class ServiceA {
    
    public String sayHello(String name) {
        return "Hello, " + name + " from ServiceA!";
    }
}
