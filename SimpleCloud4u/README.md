# SimpleCloud4u
一个云盘练手项目, 以spring为基础, 
- Thymeleaf以及JavaScript完成前端界面相关功能
- redis
  - 分享文件验证码的存储以及获取
  - 过期key触发事件
  - 存储和尝试次数超过5次的黑名单
- shiro: 验证和授权操作

## 项目

### 网盘功能
对于网盘功能, 不用数据库存储当前文件信息，而是将当前文件信息动态存储在内存中 (如果用户的小文件过多, 会不会占用过多的内存空间, 当小文件过多可以考虑使用JSON格式的文件存储当前用户的文件信息). 当用户增加文件或者删除文件动态修改当前的用户文件信息, 省去了和数据库交互的步骤. 

当用户登录的时候, 扫描当前登录用户文件夹下的全部文件,并且建立相关的索引,当用户退出, 将相关用户的实体类全部置为NULL,来释放内存.

#### 安全性

1. 通过Shiro来保证账户登录的

### 分享功能

相关文件的信息主要由

## 使用到的开源项目

谢谢这些开源项目

[SpringBoot](https://github.com/spring-projects/spring-boot) 

[SpringBoot data redis](https://spring.io/projects/spring-data-redis)

[Thymeleaf](https://github.com/thymeleaf/thymeleaf) 

[redis](https://github.com/redis/redis) 

[Apache Shiro](https://github.com/apache/shiro) 

[easyUpload.js 文件上传Js库][https://github.com/funnyque/easyUpload.js] 

[xcConfirm 弹窗Js库](https://github.com/WXAlbert/xcConfirm) 

[Dropzone 文件上传展示Js库 ](https://github.com/dropzone/dropzone)

