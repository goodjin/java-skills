# ASM 项目分析

## 项目简介
ASM 是一个 Java 字节码操作和分析框架，用于动态修改类或生成字节码。作为轻量级、高性能的字节码工具，被广泛应用于各种Java框架中。

## 核心类结构

### 1. ClassVisitor（类访问者）
- **位置**: `org.objectweb.asm.ClassVisitor`
- **职责**: 访问和修改类字节码
- **方法**: `visit()`, `visitField()`, `visitMethod()`, `visitEnd()`

### 2. ClassReader（类读取器）
- **位置**: `org.objectweb.asm.ClassReader`
- **职责**: 解析字节码数组
- **功能**: 作为Visitor模式的数据源

### 3. ClassWriter（类写入器）
- **位置**: `org.objectweb.asm.ClassWriter`
- **职责**: 生成新的字节码
- **功能**: 实现ClassVisitor接口

### 4. MethodVisitor（方法访问者）
- **职责**: 访问和修改方法字节码
- **方法**: `visitCode()`, `visitInsn()`, `visitVarInsn()`, `visitJumpInsn()`

### 5. FieldVisitor（字段访问者）
- **职责**: 访问和修改字段

### 6. Opcodes
- **位置**: `org.objectweb.asm.Opcodes`
- **职责**: JVM操作码常量定义

## 设计模式

### 1. 访问者模式（Visitor）
- ClassVisitor/MethodVisitor/FieldVisitor
- 解耦数据结构与操作

### 2. 装饰器模式（Decorator）
- TraceClassVisitor 包装ClassVisitor
- CheckClassAdapter 添加校验

### 3. 适配器模式（Adapter）
- ClassAdapter 提供默认实现
- 子类可选择性覆盖

### 4. 工厂模式（Factory）
- MethodWriter创建方法字节码
- FieldWriter创建字段

## 代码技巧

### 1. 动态类生成
```java
ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "MyClass", null, "java/lang/Object", null);

MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
mv.visitCode();
mv.visitVarInsn(Opcodes.ALOAD, 0);
mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
mv.visitInsn(Opcodes.RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();

cw.visitEnd();
byte[] bytecode = cw.toByteArray();
```

### 2. 类转换（使用Adapter）
```java
public class AddFieldAdapter extends ClassVisitor {
    private String fieldName;
    private int fieldAccess;
    private String fieldDesc;

    public AddFieldAdapter(ClassVisitor cv, String fieldName, int fieldAccess, String fieldDesc) {
        super(Opcodes.ASM9, cv);
        this.fieldName = fieldName;
        this.fieldAccess = fieldAccess;
        this.fieldDesc = fieldDesc;
    }

    @Override
    public void visitEnd() {
        cv.visitField(fieldAccess, fieldName, fieldDesc, null, null);
        super.visitEnd();
    }
}
```

### 3. 字节码分析
```java
ClassReader cr = new ClassReader(bytecode);
ClassNode cn = new ClassNode();
cr.accept(cn, 0);
// 分析方法、字段等信息
List<MethodNode> methods = cn.methods;
```

### 4. 方法替换
```java
// 使用AdviceAdapter在方法前后插入代码
public class TimingAdapter extends AdviceAdapter {
    protected TimingAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM9, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "currentTimeMillis", "J");
    }

    @Override
    protected void onMethodExit(int opcode) {
        // 计算耗时
    }
}
```

## 性能优化要点

1. **COMPUTE_FRAMES vs COMPUTE_MAXS**: 根据需求选择合适的计算模式
2. **避免重复解析**: 复用ClassReader
3. **使用合适的API版本**: ASM9是最新版本
4. **使用Tree API进行复杂操作**: ClassNode/MethodNode更易于操作
5. **及时释放资源**: 大批量处理时注意内存
