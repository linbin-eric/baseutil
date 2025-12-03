# Baseutil

[![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0-green.svg)](https://central.sonatype.com/)

## 项目简介

Baseutil 是一个强大的 Java 基础工具库，专为 Java 21+ 环境设计，提供了丰富的实用工具类和功能组件。该库源自 jfireFramework 框架，涵盖了字符串处理、IO操作、反射增强、并发工具、加密解密、任务调度等多个领域，旨在简化 Java 开发中的常见任务。

## 核心特性

### 📝 字符串与格式化工具
- **StringUtil** - 字符串处理工具类
  - 十六进制转换（字节数组与十六进制字符串互转）
  - 通配符匹配（支持 * 通配符）
  - 字符串格式化（支持 `{}` 占位符和 Map 参数替换）
  - 空值检查与默认值处理

### 📂 IO 与文件处理
- **IoUtil** - 文件和流操作工具
  - 文件读写操作（支持绝对路径和类路径资源）
  - 文件夹递归删除
  - JAR 路径检测（支持 Spring Boot 和标准 Java 环境）
  - 字节流读取工具
- **CsvUtil** - CSV 文件处理
  - CSV 文件读取并映射到 Java 对象
  - 支持自定义头部名称策略（通过注解 `@CsvHeaderName` 和 `@CsvHeaderNameStrategy`）
  - 自动类型转换（支持基本类型和包装类）

### 📄 配置文件解析
- **YamlReader** - 轻量级 YAML 解析器
  - 解析 YAML 文件到结构化数据
  - 支持完整路径和层级结构两种读取方式
  - 支持字符串、列表和 Map 类型
- **IniReader** - INI 配置文件解析器
  - 读取 INI 格式配置文件
  - 支持节（Section）和键值对（Key-Value）解析

### 🔍 包扫描与类加载
- **PackageScan** - 包扫描工具
  - 扫描指定包下的所有类
  - 支持过滤规则（包含规则 `in~` 和排除规则 `out~`）
  - 支持 JAR 包和文件系统两种扫描模式
  - 支持通配符匹配

### 🔐 加密与安全
- **AesUtil** - AES 对称加密
  - 支持 16 字节密钥的 AES 加密解密
- **DesUtil** - DES 对称加密
  - DES 算法加密解密实现
- **Md5Util** - MD5 工具
  - 字符串和文件的 MD5 计算
  - 强密码哈希生成（PBKDF2WithHmacSHA1）
  - 密码验证功能
- **RSAUtil** - RSA 非对称加密
  - RSA 公钥/私钥加密解密
  - 数字签名和验证
- **Base64Tool** - Base64 编码工具
  - Base64 编码解码操作

### 🔄 反射与动态调用
- **ReflectUtil** - 增强反射工具
  - 基于 Unsafe 的底层操作
  - 类型识别（支持 30+ 种常用类型）
  - TRUSTED_LOOKUP 访问权限
  - 异常抛出增强
- **TypeUtil** - 类型工具
  - 泛型类型解析
  - 类型转换辅助
- **ValueAccessor** - 字段访问器
  - 高性能字段读写（基于 MethodHandle）
  - 自动装箱拆箱处理
  - 支持基本类型和对象类型

### ⚡ 并发与多线程
- **BitmapObjectPool** - 位图对象池
  - 基于位图的高性能对象池实现
  - 分段锁设计，减少锁竞争
  - 支持动态创建和对象复用
  - 自动容量计算（基于 CPU 核心数）
- **CycleArray** 系列 - 循环数组
  - **StrictReadCycleArray** - 严格读取的循环数组
  - **RoundReadCycleArray** - 轮询读取的循环数组
  - **IndexReadCycleArray** - 索引读取的循环数组
  - 无锁或低锁竞争设计
- **SerialLock** - 串行锁
  - 顺序执行控制
- **Sync** - 同步工具
  - 高级同步机制
  - 支持单例同步（SingleSync）

### ⏰ 任务调度
- **SimpleWheelTimer** - 时间轮定时器
  - 基于时间轮算法的高效定时器
  - 支持大量定时任务
  - 低延迟任务调度
- **Trigger** 系列 - 触发器
  - **OnceDelayTrigger** - 一次性延迟触发器
  - **RepeatDelayTrigger** - 重复延迟触发器
  - **FixDayTimeTrigger** - 固定时间触发器

### 🔧 动态编译
- **SMC（Source Model Compiler）** - 动态编译工具
  - 运行时动态生成和编译 Java 类
  - 支持 JDK 编译器和 ECJ 编译器
  - ClassModel、MethodModel、FieldModel 等模型化代码生成
  - 支持 Spring Boot 环境

### 🔢 唯一ID生成
- **Uid** 接口 - 唯一ID生成器
  - 支持字节数组、字符串、Long 类型 ID
  - 支持纯数字 ID 生成
  - 基于时间戳的 ID 生成策略

### 🎯 字节码操作
- **Bytecode** 系列 - 字节码分析与操作
  - 字节码结构解析（常量池、方法信息、字段信息）
  - 注解读取和修改
  - 支持运行时注解覆盖（OverridesAttribute）
  - 提供多种注解上下文工厂

### 🛠️ 其他实用工具
- **NumberUtil** - 数字工具
  - 数字验证
  - 2的幂次方计算
  - Log2 计算
- **Verify** - 参数校验工具
  - 空值检查
  - 布尔值检查
  - 类型匹配检查
  - 对象相等性检查
- **RuntimeJVM** - JVM 运行时工具
  - 获取 main 方法所在类
  - 获取 JAR 文件路径
  - 进程管理（启动、终止 JAR 进程）
  - JAR 热更新支持（复制并启动新 JAR）
- **VirtualThreadUtil** - 虚拟线程工具
  - 虚拟线程创建和管理
- **HashBiMap** - 双向映射
  - Key-Value 双向查询
- **MultiHashMap** - 多值 Map
  - 一个 Key 对应多个 Value
- **MurmurHash3** - MurmurHash3 算法
  - 高性能哈希计算

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cc.jfire</groupId>
    <artifactId>baseutil</artifactId>
    <version>1.0</version>
</dependency>
```

### 环境要求

- Java 21 或更高版本
- Maven 3.6+

## 使用示例

### 1. 字符串格式化

```java
// 使用占位符格式化
String result = StringUtil.format("你好，我是{}，年龄{}", "张三", 18);
// 输出: 你好，我是张三，年龄18

// 使用 Map 参数替换
Map<String, String> params = new HashMap<>();
params.put("name", "李四");
params.put("age", "20");
String result2 = StringUtil.format("你好，我是{name}，年龄{age}", params);
// 输出: 你好，我是李四，年龄20

// 十六进制转换
byte[] bytes = {0x12, 0x34, 0x56};
String hex = StringUtil.toHexString(bytes);
// 输出: 123456
```

### 2. CSV 文件读取

```java
public class Person {
    @CsvUtil.CsvHeaderName("姓名")
    private String name;

    @CsvUtil.CsvHeaderName("年龄")
    private int age;

    // getters and setters
}

// 读取 CSV 文件
try (BufferedReader reader = new BufferedReader(new FileReader("data.csv"))) {
    List<Person> persons = CsvUtil.read(reader, Person.class);
    persons.forEach(System.out::println);
}
```

### 3. YAML 配置解析

```java
String yamlContent = """
    server:
      port: 8080
      host: localhost
    database:
      url: jdbc:mysql://localhost:3306/test
      username: root
    """;

YamlReader reader = new YamlReader(yamlContent);

// 获取完整路径的 Map
Map<String, Object> map = reader.getMapWithFullPath();
System.out.println(map.get("server.port")); // 8080

// 获取层级结构的 Map
Map<String, Object> structuredMap = reader.getMapWithIndentStructure();
Map<String, Object> serverConfig = (Map) structuredMap.get("server");
System.out.println(serverConfig.get("port")); // 8080
```

### 4. 加密解密

```java
// AES 加密
byte[] key = "1234567890123456".getBytes(); // 16字节密钥
AesUtil aesUtil = new AesUtil(key);
byte[] encrypted = aesUtil.encrypt("Hello World".getBytes());
byte[] decrypted = aesUtil.decrypt(encrypted);

// MD5 计算
String md5 = Md5Util.md5Str("Hello World");
System.out.println(md5);

// 文件 MD5
String fileMd5 = Md5Util.md5(new File("test.txt"));

// 强密码哈希
String hashedPassword = Md5Util.generateStorngPasswordHash("myPassword123");
boolean isValid = Md5Util.validatePassword("myPassword123", hashedPassword);
```

### 5. 包扫描

```java
// 扫描指定包下的所有类
String[] classes = PackageScan.scan("com.example.service");

// 使用过滤规则：只包含 Controller
String[] controllers = PackageScan.scan("com.example:in~*Controller");

// 使用过滤规则：排除 Test 类
String[] nonTestClasses = PackageScan.scan("com.example:out~*Test");
```

### 6. 对象池

```java
// 创建对象池
BitmapObjectPool<MyObject> pool = new BitmapObjectPool<>(
    index -> new MyObject(index),
    1000  // 容量
);

// 获取对象
MyObject obj = pool.acquire();
if (obj != null) {
    try {
        // 使用对象
        obj.doSomething();
    } finally {
        // 归还对象
        pool.release(obj.getIndex());
    }
}

// 查看可用数量
int available = pool.getAvailableCount();
```

### 7. 时间轮定时器

```java
// 创建定时器
ExecutorService executor = Executors.newFixedThreadPool(4);
SimpleWheelTimer timer = new SimpleWheelTimer(executor, 100); // 100ms tick

// 添加一次性任务
timer.add(new OnceDelayTrigger(() -> {
    System.out.println("延迟任务执行");
}, 1000)); // 1秒后执行

// 添加重复任务
timer.add(new RepeatDelayTrigger(() -> {
    System.out.println("重复任务执行");
}, 500, 2000)); // 延迟500ms，每2秒重复一次
```

### 8. 反射增强

```java
// 使用 ValueAccessor 高性能字段访问
Field field = MyClass.class.getDeclaredField("name");
ValueAccessor accessor = ValueAccessor.standard(field);

MyClass obj = new MyClass();
accessor.setObject(obj, "新值");
String value = (String) accessor.getObject(obj);

// 类型判断
Class<?> clazz = String.class;
int classId = ReflectUtil.getClassId(clazz);
boolean isPrimitive = ReflectUtil.isPrimitive(int.class); // true
```

### 9. 参数校验

```java
public void processUser(User user) {
    // 非空检查
    Verify.notNull(user, "用户对象不能为空");
    Verify.notNull(user.getName(), "用户名不能为空");

    // 布尔检查
    Verify.True(user.getAge() > 0, "年龄必须大于0");

    // 类型匹配
    Verify.matchType(user, User.class, "对象类型不匹配");
}
```

### 10. JVM 运行时工具

```java
public class Main {
    public static void main(String[] args) {
        // 注册主类
        RuntimeJVM.registerMainClass(args);

        // 获取 JAR 所在目录
        File jarDir = RuntimeJVM.getDirOfMainClass();
        System.out.println("JAR 目录: " + jarDir);

        // 检测是否在 JAR 中运行
        boolean inJar = RuntimeJVM.detectRunningInJar();

        // 启动另一个 JAR
        long pid = RuntimeJVM.startJar("/path/to/app.jar", "--port", "8080");
        System.out.println("新进程 PID: " + pid);
    }
}
```

## 应用场景

### 🌐 Web 应用开发
- 配置文件解析（YAML、INI）
- 参数校验和格式化
- 加密解密（用户密码、敏感数据）
- 包扫描（自动注册组件）

### 🔄 微服务与分布式系统
- 唯一ID生成（分布式ID）
- 对象池优化性能
- 时间轮定时器（定时任务）
- JVM 运行时管理（热更新）

### 📊 数据处理
- CSV 文件导入导出
- 文件 MD5 校验
- 批量数据处理（对象池复用）

### 🎮 动态功能扩展
- 动态编译（插件系统）
- 字节码操作（AOP、运行时增强）
- 反射增强（框架开发）

### 🚀 高性能场景
- 无锁或低锁并发数据结构
- 高性能对象池
- 时间轮定时器（百万级任务）

## 依赖说明

### 核心依赖
- **unsafe-accessor** (1.7.0) - Unsafe 操作封装
- **slf4j-api** (2.0.6, provided) - 日志门面

### 可选依赖
- **poi-ooxml** (5.4.1, optional) - Excel 文件处理
- **ecj** (3.40.0, optional) - Eclipse Java 编译器
- **lombok** (1.18.34, provided) - 简化代码

## 许可证

本项目采用 [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.txt) 许可证。

## 作者

- **Lin Bin**
- Email: 495561397@qq.com
- Gitee: [https://gitee.com/eric_ds/baseutil](https://gitee.com/eric_ds/baseutil)

## 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进项目！

## 更新日志

### Version 1.0
- 初始版本发布
- 支持 Java 21
- 提供 50+ 实用工具类和组件
- Maven 中央仓库发布

---

如有任何问题或建议，请通过 [Issue](https://gitee.com/eric_ds/baseutil/issues) 反馈。
