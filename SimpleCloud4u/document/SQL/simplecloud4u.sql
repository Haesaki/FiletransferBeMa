CREATE TABLE `user`
(
    `id`       int unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `name`     varchar(50) DEFAULT '' COMMENT '用户名',
    `email`    varchar(50) DEFAULT '1@sc4u.xyz' COMMENT '用户邮箱',
    `password` varchar(20) DEFAULT '' COMMENT '密码',
    `role`     tinyint(1) DEFAULT '0' COMMENT '用户角色,0管理员，1普通用户',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3