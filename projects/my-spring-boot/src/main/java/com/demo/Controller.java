package com.demo;

/**
 * 示例 Controller
 */
@MyComponent
public class Controller {
    
    @MyAutowired
    private ServiceB serviceB;
    
    public void handleRequest() {
        System.out.println("Controller handling request...");
        serviceB.test();
    }
}
