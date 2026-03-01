# gson

## 项目简介
# Gson

Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object.
Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of.

There are a few open-source projects that can convert Java objects to JSON. However, most of them require that you place Java annotations in your classes; something that you can not do if you do not have access to the source-code. Most also do not fully support the use of Java Generics. Gson considers both of these as very important design goals.

> [!NOTE]\
> Gson is currently in maintenance mode; existing bugs will be fixed, but large new features will likely not be added. If you want to add a new feature, please first search for existing GitHub issues, or create a new one to discuss the feature and get feedback.

> [!IMPORTANT]\
> Gson's main focus is on Java. Using it with other JVM languages such as Kotlin or Scala might work fine in many cases, but language-specific features such as Kotlin's non-`null` types or constructors with default arguments are not supported. This can lead to confusing and incorrect behavior.\
> When using languages other than Java, prefer a JSON library with explicit support for that language.

> [!IMPORTANT]\
> Gson is not a recommended library for interacting with JSON on Android. The open-ended reflection in the Gson runtime doesn't play nicely with shrinking/optimization/obfuscation passes that Android release apps should perform.\
> If your app or library may be running on Android, consider using [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/basic-serialization.md#basics) or [Moshi's Codegen](https://github.com/square/moshi?tab=readme-ov-file#codegen),
> which use code generation instead of reflection. This avoids Gson's runtime crashes when optimizations are applied (usually due to the fields missing or being obfuscated), and results in faster performance on Android devices.
> The Moshi APIs may be more familiar to users who already know Gson.
> If you still want to use Gson and attempt to avoid these crashes, you can see how to do so [here](Troubleshooting.md#proguard-r8).

### Goals
  * Provide simple `toJson()` and `fromJson()` methods to convert Java objects to JSON and vice-versa
  * Allow pre-existing unmodifiable objects to be converted to and from JSON
  * Extensive support of Java Generics
  * Allow custom representations for objects
  * Support arbitrarily complex objects (with deep inheritance hierarchies and extensive use of generic types)

### Download

Gradle:
```gradle
dependencies {
  implementation 'com.google.code.gson:gson:2.13.2'
}
```

Maven:
```xml
<dependency>

## 整体架构描述


## 核心模块划分
Despite supporting older Java versions, Gson also provides a JPMS module descriptor (module name `com.google.gson`) for users of Java 9 or newer.

#### JPMS dependencies (Java 9+)
These are the optional Java Platform Module System (JPMS) JDK modules which Gson depends on.
This only applies when running Java 9 or newer.

- `java.sql` (optional since Gson 2.8.9)\
When this module is present, Gson provides default adapters for some SQL date and time classes.

- `jdk.unsupported`, respectively class `sun.misc.Unsafe` (optional)\
When this module is present, Gson can use the `Unsafe` class to create instances of classes without no-args constructor.
However, care should be taken when relying on this. `Unsafe` is not available in all environments and its usage has some pitfalls,
see [`GsonBuilder.disableJdkUnsafe()`](https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html#disableJdkUnsafe()).

#### Minimum Android API level

- Gson 2.11.0 and newer: API level 21
- Gson 2.10.1 and older: API level 19

Older Gson versions may also support lower API levels, however this has not been verified.

### Documentation
  * [API Javadoc](https://www.javadoc.io/doc/com.google.code.gson/gson): Documentation for the current release
  * [User guide](UserGuide.md): This guide contains examples on how to use Gson in your code
  * [Troubleshooting guide](Troubleshooting.md): Describes how to solve common issues when using Gson

## 技术选型
- 构建工具: Maven
- 主要语言: Java

## 与PRD的对应关系
参见项目官方文档
