#!/bin/bash

echo "=== SpringBootCompiler 性能对比测试 ==="

cd /Users/linbin/Documents/代码/base2

# 获取类路径
CLASSPATH="target/classes:target/test-classes:$(head -n 1 classpath.txt)"

# 运行性能测试
echo "运行性能测试..."
java -cp "$CLASSPATH" com.jfirer.baseutil.SimplePerformanceTest

echo "测试完成"