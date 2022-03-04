# filetransferbema(File Transfer between Machine)

## 介绍
Netty的文件服务器和客户端，利用其他方式支持网页文件的上传和下载，文件上传校验以及大文件传输, 以及部分网盘功能

主要是想要实践下下Netty的文件传输功能, 并且结合一些其他的开源项目实现利用ssh在服务器和主机之间传输和部分管理文件的功能

### 痛点
现在在主机和服务器之间传输文件不是很方便
1. ssh传输文件要输入类似下面的命令(记不住啊, 很难一次性记住那些参数,例如主机ip地址以及验证文件位置)
```shell
scp [-1246BCpqrv] [-c cipher] [-F ssh_config] [-i identity_file]
[-l limit] [-o ssh_option] [-P port] [-S program]
[[user@]host1:]file1 [...] [[user@]host2:]file2
```
2. 有些软件例如xftp,虽然很好用,但是只有传输文件功能和免费的家庭/学校许可,总有点不爽

## 设计

这个项目可以分成三部分

1. Netty File: 通过Netty实现的客户端和服务器端的大文件传输功能的模块
2. SSH Client: 基于mona-ssh以及spring实现在本地开启一个客户端 (只有客户端)
   1. 实现通过ssh密钥认证连接服务器 (**其实还有一个思路, 就是在程序跑在服务器端, 通过ssh密钥认证, 如果验证通过了就能访问权限受限的目录,然后直接在网页操作范围受限的目录,并且限制上传文件大小**)
   2. 客户端和服务器通过scp命令实现**文件的上传和下载**
   3. 客户端可以在网页**执行**受限的**linux 命令** , (直接就在后端验证命令能不能执行, 例如 rm 一定范围的文件夹下的文件, ls 一定范围的文件夹下的文件, mv 等等操作)
3. SimpleCloud4u: 网盘程序, 主要支持文件的上传并且一键分享, 得到连接和验证码.
   1. 文件上传后,自动生成链接和随机的验证码
   2. 文件下载需要正确的链接和正确的验证码才能正确获取文件
   3. 用户管理, 通过一次性的邀请码才能成功注册账户,并且上传的文件收到配置文件中参数的限制



## 难点

1. 系统的安全性
2. 高并发访问怎么处理(虽然这个不会遇到,极小的可能性会遇到)

## 实现

## 使用到的开源工具 (感谢这些开源库)
[Netty](https://netty.io/)
[Thymeleaf](https://docs.spring.io/spring-boot/docs/2.6.3/reference/htmlsingle/#boot-features-spring-mvc-template-engines)
[Spring-Boot](https://docs.spring.io/spring-boot)
[MyBatis Framework](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)