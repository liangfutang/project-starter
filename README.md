<center>starter</center>   
# 一. 概述
本项目作为基础功能性服务，为业务提供基础支撑，充分解放您的双手，一次引用，全局利用，净化您的项目，让业务服务纯业务。使用后会让您拥有爽到爆炸的服务体验。为方便使用，后期会打成jar包放到github上，以供直接使用。由于各自项目中使用的配置存放不一致，如apollo或其他配置中心等，本项目为充分解耦合，会读取引用方项目中的properties文件。

# 二. mybatis plus接入
## 2.1 功能模块介绍
> 接入

## 2.2 接入方法
在主pom中的\<dependencyManagement\>引入
```java
<dependency>
    <groupId>com.zjut.spring.boot</groupId>
    <artifactId>factory-spring-boot-dependencies</artifactId>
    <version>${starter.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```
子pom中引入
```java
<dependencies>
    <dependency>
        <groupId>com.zjut.spring.boot</groupId>
        <artifactId>factory-spring-boot-starter-jdbc</artifactId>
    </dependency>
</dependencies>
```
在引用方resources文件夹下添加properties\datasource.properties文件，如使用其他配置中心，此文件可以省去，但是本start项目需要做些许修改
