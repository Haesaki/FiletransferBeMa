# Spring Data Redis

## Redis app


## 默认使用的序列化工具
> By default, RedisCache and RedisTemplate are configured to use Java native serialization. Java native serialization is known for allowing the running of remote code caused by payloads that exploit vulnerable libraries and classes injecting unverified bytecode. Manipulated input could lead to unwanted code being run in the application during the deserialization step. As a consequence, do not use serialization in untrusted environments. In general, we strongly recommend any other message format (such as JSON) instead.

Java原生的序列化方式存在远程代码执行漏洞！

### 序列化方式的比较

[部分来源于](https://segmentfault.com/a/1190000039934578) 

#### 原生

只需要实现java.io.Serializable / java.io.Externalizable, 就可以使用Java自带的序列化方式，实现序列化接口只是表明可以被序列化和反序列化，还需要借助I/O操作的ObjectInputStream和ObjectOutputStream对对象进行序列化和反序列化。

##### 通用性

不支持跨语言的序列化和反序列化

##### 易用性

但是JDK Serializable在使用上相比开源框架难用许多，可以看到上面的编解码使用非常生硬，需要借助ByteArrayOutputStream和ByteArrayInputStream才可以完整字节的转换。

##### 可扩展性

JDK Serializable中通过serialVersionUID控制序列化类的版本，如果序列化与反序列化版本不一致，则会抛出java.io.InvalidClassException异常信息，提示序列化与反序列化SUID不一致。

##### 性能

| 1000 万序列化耗时 ms | 1000万反序列化耗时 ms |
| -------------------- | --------------------- |
| 38952                | 96508                 |

#### Kryo

Kryo一个快速有效的Java二进制序列化框架，它依赖底层ASM库用于字节码生成，因此有比较好的运行速度。Kryo的目标就是提供一个序列化速度快、结果体积小、API简单易用的序列化框架。Kryo支持自动深/浅拷贝，它是直接通过对象->对象的深度拷贝，而不是对象->字节->对象的过程。

##### 通用性

Java

##### 易用性

Input和output封装了所有的流操作

##### 扩展性

默认的序列化类FiledSerializer不支持字段扩展，如果想使用扩展序列化方法，需要配置其他默认序列化器

##### 性能

| 1000 万序列化耗时 ms | 1000万反序列化耗时 ms |
| -------------------- | --------------------- |
| 13550                | 14315                 |

##### Protocol Buffer

需要预先定义Schema

##### 通用性

protobuf设计之初就是设计一款和语言无关的序列化框架，支持Java，python，C++，GO，C#

##### 易用性

protobuf需要使用IDL来定义Schema描述文件，定义完描述文件后，直接使用protoc来直接生成序列化和反序列化代码，使用上只需要简单编写描述文件，就可以使用protobuf

##### 可扩展性

新增字段：一定要保证新增字段要有对应的默认值，这样才能和旧代码间交互，相应的新协议生成的消息，可以被就协议解析

删除字段：通过reserved删除

##### 性能

| 1000 万序列化耗时 ms | 1000万反序列化耗时 ms |
| -------------------- | --------------------- |
| 14235                | 30694                 |

##### Fast Json