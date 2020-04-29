<center>starter</center>   

[toc]

# 一. 概述
本项目作为基础功能性服务，为业务提供基础支撑，充分解放您的双手，一次引用，全局利用，净化您的项目，让业务服务纯业务。使用后会让您拥有爽到爆炸的服务体验。为方便使用，后期会打成jar包放到github上，以供直接使用。由于各自项目中使用的配置存放不一致，如apollo或其他配置中心等，本项目为充分解耦合，会读取引用方项目中的properties文件。

# 二. mybatis plus接入
## 2.1 功能模块介绍
> 接入该模块后不需要调用方再集成mybatis plus了，对于之前接入的是mybatis的同样适用,完美兼容。
默认接入了分页插件，否则使用的不是物理分页，会导致内存飙高或oom的风险。
本模块的分页保持对mybatis plus分页的兼容。对于没有使用mybatis plus分页Ipage的，如自己的xml文件中的SQL，本模块会自动加上预先设置的分页，如传入的SQL中有分页，本模块会对传入单次查询大小做判断，如超过预设的阈值，会将该分页大小改为阈值，限制单次查询的量，否则可能会导致查询慢，长时间占用数据库宝贵的连接。另，该阈值支持动态修改。

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
