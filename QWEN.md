# Qwen Code Context: BaseUtil Java Library

This document provides context for the `baseutil` Java library, a high-performance utility library designed to provide foundational functionalities for the `jfireFramework` project.

## Project Overview

- **Name**: BaseUtil
- **Group ID**: com.jfirer
- **Artifact ID**: baseutil
- **Version**: 1.1.12-SNAPSHOT
- **Language**: Java 21+
- **Build Tool**: Maven
- **License**: GNU Affero General Public License v3.0
- **Description**: A high-performance Java utility library offering functionalities like string processing, concurrency tools, encryption/decryption, reflection, bytecode parsing, file handling, scheduling, and unique ID generation.

## Core Functional Modules

### 1. String Utilities (`com.jfirer.baseutil.STR`, `com.jfirer.baseutil.StringUtil`)
- **STR.java**: Template-based string formatting supporting positional (`{}`) and named (`${name}`) placeholders. Also supports appending exception stack traces.
- **StringUtil.java**: Utilities for hex conversion, pattern matching, parameter formatting, and general string manipulation.

### 2. Concurrency Tools (`com.jfirer.baseutil.concurrent`)
- **BitmapObjectPool.java**: A high-performance object pool based on bitmaps, supporting lazy initialization and thread safety.
- **CycleArray.java and implementations**: Cycle array collection implementations with various read strategies.
- **SerialLock.java**, **Sync.java**: Synchronization and locking utilities.

### 3. Encryption & Decryption (`com.jfirer.baseutil.encrypt`)
- **AesUtil.java**, **DesUtil.java**: Symmetric encryption/decryption.
- **RSAUtil.java**: Asymmetric encryption/decryption.
- **Md5Util.java**: Hashing algorithms.
- **Base64Tool.java**: Base64 encoding/decoding.

### 4. Reflection & Bytecode (`com.jfirer.baseutil.reflect`, `com.jfirer.baseutil.bytecode`)
- **Bytecode Parsing (`com.jfirer.baseutil.bytecode`)**: A complete framework for parsing Java bytecode, analyzing class file structures.
- **ReflectUtil.java**: Enhanced reflection utilities, including field accessors and type identification.
- **Value Accessors (`com.jfirer.baseutil.reflect.valueaccessor`)**: High-performance field access mechanisms (e.g., `UnsafeValueAccessorImpl`, `LambdaAccessorImpl`).

### 5. File Handling (`com.jfirer.baseutil`)
- **CsvUtil.java**: High-performance CSV file reading, supporting annotation mapping and custom header strategies.
- **IniReader.java**, **YamlReader.java**: Utilities for parsing INI and YAML configuration files.

### 6. Scheduling (`com.jfirer.baseutil.schedule`)
- **SimpleWheelTimer.java**: Time-wheel based timer for task scheduling.
- **Trigger implementations**: Various trigger strategies (once, repeat, fixed time).

### 7. Unique ID Generation (`com.jfirer.baseutil.uniqueid`)
- **SpringId.java**, **SummerId.java**, **AutumnId.java**, **WinterId.java**: Time-based unique ID generators.

### 8. Dynamic Compilation (`com.jfirer.baseutil.smc`)
- **SmcHelper.java**: In-memory Java code compilation and class loading utilities.

## Key Dependencies

- **io.github.karlatemp:unsafe-accessor**: Provides safe access to the `Unsafe` class.
- **org.projectlombok:lombok** (provided): For code generation (e.g., `@Data`).
- **org.openjdk.jmh:jmh-generator-annprocess** (test): For performance benchmarking.
- **com.microsoft.playwright:playwright** (optional/provided): For browser automation support.

## Building, Running, and Testing

- **Build**: `mvn compile`
- **Test**: `mvn test`
- **Generate Sources JAR**: `mvn source:jar`
- **Generate Javadoc JAR**: `mvn javadoc:jar`

## Development Conventions

- **Java Version**: Java 21 is required.
- **Performance**: Heavy emphasis on performance optimization techniques like zero-copy string handling, bit operations, lazy initialization, and segmented locks.
- **Lombok**: Used for reducing boilerplate code (e.g., getters, setters, `toString`).
- **JMH**: Benchmarks are included for critical components.
- **Reflection**: Custom, high-performance reflection utilities are preferred over standard Java reflection where possible.

## Module Structure (Key Packages)

```
com.jfirer.baseutil/
├── bytecode/           # Bytecode parsing and processing
├── concurrent/         # Concurrency tools and object pools
├── encrypt/            # Encryption and decryption utilities
├── exception/          # Custom exceptions
├── reflect/            # Reflection and dynamic access
├── schedule/           # Task scheduling
├── smc/                # Dynamic compilation
├── time/               # Time utilities
└── uniqueid/           # Unique ID generation
```