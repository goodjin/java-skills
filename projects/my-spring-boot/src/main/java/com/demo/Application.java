package com.demo;

/**
 * 启动类
 */
public class Application {
    
    public static void main(String[] args) {
        MySpringApplication.run(Application.class, args);
        
        // 演示调用
        System.out.println("\n=== Running Application ===");
        Controller controller = new Controller();
        controller.handleRequest();
    }
}
