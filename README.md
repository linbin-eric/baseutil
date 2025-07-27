# BaseUtil

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.0+-green.svg)](https://maven.apache.org/)

一个为 jfireFramework 提供基础功能的高性能 Java 工具库，包含字符串处理、并发工具、加密解密、反射操作、字节码解析等多个模块。

## 项目信息

- **Group ID**: com.jfirer
- **Artifact ID**: baseutil
- **Version**: 1.1.12-SNAPSHOT
- **Java Version**: 21+
- **License**: GNU Affero General Public License v3.0

## 核心功能模块

### 🔤 字符串工具
- **STR**: 模板化字符串格式化工具，支持占位符替换和异常堆栈追加
- **StringUtil**: 字符串处理工具集，包含十六进制转换、模式匹配、参数格式化等功能

### 🧵 并发工具
- **BitmapObjectPool**: 基于位图的高性能对象池，支持延迟初始化和多线程安全
- **CycleArray**: 循环数组实现集合，提供多种读取策略
- **SerialLock**: 串行锁实现
- **Sync**: 同步工具

### 🔐 加密解密
- **AES/DES工具**: 对称加密解密实现
- **RSA工具**: 非对称加密解密实现
- **MD5工具**: 哈希算法实现
- **Base64工具**: Base64编解码

### 🔍 反射与字节码
- **字节码解析**: 完整的Java字节码解析框架，支持类文件结构分析
- **反射工具**: 增强反射操作，包含字段访问器、类型工具等
- **注解处理**: 运行时注解元数据处理

### 📄 文件处理
- **CSV工具**: 高性能CSV文件读取，支持注解映射和自定义头部策略
- **INI读取器**: INI配置文件解析
- **YAML读取器**: YAML文件处理

### ⏰ 定时任务
- **轮询定时器**: 基于时间轮的定时任务调度
- **触发器**: 多种触发策略支持（一次性、重复、固定时间）

### 🆔 唯一ID生成
- **季节ID生成器**: 基于时间的唯一ID生成（SpringId, SummerId, AutumnId, WinterId）

### 🏗️ 动态编译
- **SMC编译器**: 内存中Java代码编译和类加载

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.jfirer</groupId>
    <artifactId>baseutil</artifactId>
    <version>1.1.12-SNAPSHOT</version>
</dependency>
```

### 使用示例

#### 字符串格式化
```java
// 使用STR进行模板格式化
String result = STR.format("Hello {}, age is {}", "World", 25);

// 使用Map进行命名参数格式化
Map<String, Object> params = Map.of("name", "John", "age", 30);
String result2 = STR.format("Hello ${name}, age is ${age}", params);
```

#### 对象池使用
```java
// 创建字符串对象池
BitmapObjectPool<String> pool = new BitmapObjectPool<>(
    index -> "Object-" + index, 
    1000
);

// 获取对象
String obj = pool.acquire();
// 使用对象...
// 释放对象
pool.release(obj.hashCode());
```

#### CSV文件处理
```java
public class Person {
    @CsvHeaderName("姓名")
    private String name;
    private int age;
    // getters and setters...
}

// 读取CSV文件
List<Person> persons = CsvUtil.read(reader, Person.class);
```

#### 加密解密
```java
// AES加密
String encrypted = AesUtil.encrypt("hello world", "your-secret-key");
String decrypted = AesUtil.decrypt(encrypted, "your-secret-key");

// RSA加密
KeyPair keyPair = RSAUtil.generateKeyPair();
String encrypted2 = RSAUtil.encrypt("hello", keyPair.getPublic());
String decrypted2 = RSAUtil.decrypt(encrypted2, keyPair.getPrivate());
```

## 性能特性

- **零拷贝字符串处理**: 基于char数组的高效字符串操作
- **位运算优化**: 大量使用位运算提升性能
- **延迟初始化**: 对象池支持延迟创建，减少内存占用
- **多线程优化**: 分段锁设计，减少锁竞争
- **JMH性能测试**: 内置性能基准测试

## 模块结构

```
com.jfirer.baseutil/
├── bytecode/           # 字节码解析和处理
├── concurrent/         # 并发工具和对象池
├── encrypt/           # 加密解密工具
├── exception/         # 自定义异常
├── reflect/           # 反射和动态访问
├── schedule/          # 定时任务调度
├── smc/              # 动态编译
├── time/             # 时间工具
└── uniqueid/         # 唯一ID生成
```

## 第三方依赖

- **Unsafe-Accessor**: 提供安全的Unsafe操作
- **Playwright**: 可选的浏览器自动化支持
- **Lombok**: 代码生成和简化
- **JMH**: 性能基准测试

## 构建和测试

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 生成源码JAR
mvn source:jar
```

## 许可证

本项目采用 GNU Affero General Public License v3.0 许可证。详见 [LICENSE](https://www.gnu.org/licenses/agpl-3.0.txt)。

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 项目地址

- 源码地址: http://git.oschina.net/eric_ds