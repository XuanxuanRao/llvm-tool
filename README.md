在 releases 下载 [client.jar](https://github.com/XuanxuanRao/llvm-tool/releases/download/v1/client.jar)，在任意位置使用命令行运行 jar 包

```shell
java -jar client.jar path_to_llvm path_to_input [path_to_output](可选)
```

- path_to_llvm 为 llvm 代码的路径
- path_to_input 为输入内容的路径
- path_to_output 为期望的输出路径，以文件保存，如果缺省输出到标准输出

比如：

```shell
java -jar client.jar llvm_ir.txt test/A/testcase1/in.txt output.txt
```

