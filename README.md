# iBlog 后端服务说明文档

本项目是一个前后端分离的个人博客系统 —— **iBlog** 的后端部分，实现了完整的用户体系、文章体系、评论体系以及点赞收藏体系，并结合SpringBoot框架 Redis、JWT、Mybatis-plus、Spring Security 等技术构建了一个较为完善的博客项目。

项目已部署在阿里云 ECS 的 Docker 环境中，并可通过公网访问：

注：因为SSL证书到期，已经不能访问。可以在根目录下的iblog.png查看原页面效果
  
* **原公网访问地址**：[https://www.yellow-iblog.cn](https://www.yellow-iblog.cn)
  
* **前端Vue项目仓库地址**：https://github.com/Yello666/iBlog_Front

可以使用默认账户登录

用户名：123

密码：123



## 项目简介

**iBlog 后端服务**基于 Spring Boot 开发，提供博客系统所需的主要业务能力，包括：

### 用户相关功能

* 用户注册
* 用户登录（JWT 鉴权）
* 修改用户信息
* 修改密码
* 注销用户

### 文章相关功能

* 查看某一篇文章(已设置redis缓存功能)
* 发布文章(必须登陆后使用)
* 修改文章(必须登陆后使用且只能修改自己发布的文章)
* 删除文章(必须登陆后使用且只能删除自己发布的文章)
* 查看点赞数最高的5篇文章
* 查看当前用户的个人文章列表

### 评论相关功能

* 查看某篇文章的评论(已设置redis缓存)
* 发表评论
* 点赞评论

> 注：暂不支持回复评论的功能


### 点赞与收藏

* 点赞文章
* 收藏文章
* 点赞评论

点赞与收藏逻辑均使用 Redis ，并定期同步至mysql数据库。

由于在同步点赞数到mysql的同时，还要保证查询文章时返回的点赞数的正确性、保证用户点赞状态的正确性，这个是本人做得最辛苦的地方。



## 权限与安全

项目的所有接口均已实现权限控制：

* **JWT**：用户登录后颁发 Token，用于身份校验
* **Spring Security**：进行接口权限验证
* **Redis**：存储登录用户信息



## 技术栈与架构

* **后端框架**：Spring Boot
* **安全认证**：JWT + Spring Security
* **缓存中间件**：Redis
* **数据库中间件**：MyBatis-plus
* **日志框架**：SLF4J
* **部署方式**：Docker（部署在阿里云 ECS）



## 部署情况

后端服务已打包成 Docker 镜像并部署至阿里云 ECS，可通过前端页面在公网正常访问。



## 项目亮点

* 完整的用户/文章/评论体系，且配备了可公网访问的前端展示页面
* 使用Redis zset实现了热门文章实时推荐功能，热度高（点赞、浏览、评论、收藏的加权）的文章会优先展示在前列
* 使用Redis set优化点赞收藏逻辑，将点赞和取消点赞合并为一个接口，接口响应时间降低55%，性能提升128%
* Redis 缓存热点文章提升访问性能
* JWT 统一登录认证与权限校验
* Spring Security 全接口权限校验
* 前后端分离，可轻松扩展



## 项目结构

```
├── common           # 共用工具类
├── config           # Security、JWT、跨域等配置
├── controller       # 控制层
├── exception        # 异常类与异常捕获
├── jwt              # jwt工具类
├── mapper           # DAO 层
├── model            # 实体类和请求类和响应类
├── service          # 业务层
└── iBLOGApplication # 启动类
```
## 压测

环境：2核8GB的云主机上使用jmeter对点赞接口进行压测，并发线程数50，持续5分钟。

场景：准备了200个不同的测试用户，随机选择50个用户随机对3、5、7、8、13号文章点赞或取消点赞。

结果：
summary +    213 in 00:00:16 =   13.2/s Avg:  1666 Min:   371 Max:  3371 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary +   1184 in 00:00:30 =   39.4/s Avg:  1250 Min:   189 Max:  4186 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   1397 in 00:00:46 =   30.3/s Avg:  1314 Min:   189 Max:  4186 Err:     0 (0.00%)

summary +   1527 in 00:00:30 =   50.9/s Avg:   979 Min:   101 Max:  2407 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   2924 in 00:01:16 =   38.4/s Avg:  1139 Min:   101 Max:  4186 Err:     0 (0.00%)

summary +   1495 in 00:00:30 =   49.8/s Avg:   990 Min:   167 Max:  2665 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   4419 in 00:01:46 =   41.6/s Avg:  1089 Min:   101 Max:  4186 Err:     0 (0.00%)

summary +   1781 in 00:00:30 =   59.3/s Avg:   834 Min:   135 Max:  2250 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   6200 in 00:02:16 =   45.5/s Avg:  1015 Min:   101 Max:  4186 Err:     0 (0.00%)

summary +   1763 in 00:00:30 =   58.8/s Avg:   841 Min:   108 Max:  2650 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   7963 in 00:02:46 =   47.9/s Avg:   977 Min:   101 Max:  4186 Err:     0 (0.00%)

summary +   1997 in 00:00:30 =   66.6/s Avg:   745 Min:    89 Max:  1925 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =   9960 in 00:03:16 =   50.8/s Avg:   930 Min:    89 Max:  4186 Err:     0 (0.00%)

summary +   2089 in 00:00:30 =   69.6/s Avg:   708 Min:    92 Max:  2099 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =  12049 in 00:03:46 =   53.3/s Avg:   892 Min:    89 Max:  4186 Err:     0 (0.00%)

summary +   2271 in 00:00:30 =   75.7/s Avg:   656 Min:    94 Max:  1666 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =  14320 in 00:04:16 =   55.9/s Avg:   854 Min:    89 Max:  4186 Err:     0 (0.00%)

summary +   2379 in 00:00:30 =   79.3/s Avg:   625 Min:    81 Max:  2253 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0

summary =  16699 in 00:04:46 =   58.4/s Avg:   822 Min:    81 Max:  4186 Err:     0 (0.00%)

summary +   1167 in 00:00:14 =   81.0/s Avg:   614 Min:    63 Max:  1656 Err:     0 (0.00%) Active: 0 Started: 50 Finished: 50

summary =  17866 in 00:05:01 =   59.4/s Avg:   808 Min:    63 Max:  4186 Err:     0 (0.00%)

Tidying up ...    @ 2026 Mar 16 23:18:14 CST (1773674294413)
... end of run

本次压测采用单机模式（JMeter 与被测服务同部署于 2C8G 服务器），压测工具本身消耗了部分宿主机资源。因此，上述测试结果（TPS ~59/s, Avg RT ~808ms）为保守估计值，实际分离部署压测机后性能表现预计会更优。

注：因为没有购买多一台同网段的云主机，并且在本地压测云主机会在并发数为30左右时，被阿里云网关给限流，导致请求会报502错误，因此没有很好的分机压测环境，所以在云主机上使用Jmeter进行压测了。压测时CPU最高负载率达到99.2%，属于高压状态，所以请求时间长。如果使用本地Jmeter压测云主机，50并发的情况下，最高TPS为31左右，约10%的请求会被网关拦截报502错误，CPU空闲率为97%左右。

### 压测亮点总结

1.零错误率：全程处理 17,866 次请求，错误率为 0%，点赞接口稳定性高。

2.性能稳步提升：TPS（吞吐量）从初期的 13.2/s 逐步攀升至峰值 81.0/s，平均 TPS 保持在 59.4/s。

---

