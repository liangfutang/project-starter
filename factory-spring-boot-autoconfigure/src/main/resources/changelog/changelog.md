# 说明
## 时间: 2020-03-18
## 原因
因为大量的改版，技术方案的革新，到后期的时候，会经常忘记改版之前的内容或原因啥的，现开此changelog来记录每次改版的相关

---
---
---

# 2020-03-18 dubbo模块的添加
## 说明
> 经过昨天的奋战，终于在凌晨后将该dubbo模块添加成功，并测试通过，可喜可乐。
## 后续优化计划
> 当前对提供出去的接口的扫描包的添加是通过在启动类上添加@DubboComponentScan注解的方式，感觉还是没做到依赖既使用，后续需要研究将该扫描包添加的方式也集成到starter中去。
## 使用说明
> 1. 在主pom的dependencyManagement中添加
```
<dependency>
    <groupId>com.zjut.spring.boot</groupId>
    <artifactId>factory-spring-boot-dependencies</artifactId>
    <version>${starter.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```
> 在子pom中添加
```
<dependency>
    <groupId>com.zjut.spring.boot</groupId>
    <artifactId>factory-spring-boot-starter-dubbo</artifactId>
</dependency>
```
> 2. 在启动类上添加注解`@DubboComponentScan(basePackages = {"com.zjut.factory.provider.openservice.service.impl"})`

# 没有引用所有starter模块的情况下，应用方启动报错
## 起因
> starter集成的相关模块都是需要每个服务各自的特定参数的，可以通过apollo、cloud配置中心等，本架构设计通过在服务中添加配置文件properties方式，然后在starter中读取。本次starter集成dubbo模块后，在factory-provider服务启动正常，因为该服务中有jdbc和dubbo的相关服务配置文件，所以，启动正常。在factory-consumer集成starter的dubbo模块前启动了一次，发现报错了，log提示找不到dubbo配置文件。
## 修改方案
> 在寻找配置文件的注解`@PropertySource`中添加配置属性`ignoreResourceNotFound = true`，在找不到该文件的时候不会报错。再次启动factory-consumer的时候不再报错了