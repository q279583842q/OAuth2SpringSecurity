# 一、OAuth2.0介绍

## 1.概念说明
&emsp;&emsp;先说OAuth，OAuth是Open Authorization的简写。
&emsp;&emsp;OAuth协议为用户资源的授权提供了一个安全的、开放而又简易的标准。与以往的授权方式不同之处是OAuth的授权不会使第三方触及到用户的帐号信息（如用户名与密码），即第三方无需使用用户的用户名与密码就可以申请获得该用户资源的授权，因此OAuth是安全的。
&emsp;&emsp;`OAuth2.0`是OAuth协议的延续版本，但不向前兼容(即完全废止了OAuth1.0)。

## 2.使用场景
&emsp;&emsp;假设，A网站是一个打印照片的网站，B网站是一个存储照片的网站，二者原本毫无关联。如果一个用户想使用A网站打印自己存储在B网站的照片，那么A网站就需要使用B网站的照片资源才行。按照传统的思考模式，我们需要A网站具有登录B网站的用户名和密码才行，但是，现在有了OAuth2，只需要A网站获取到使用B网站照片资源的一个通行令牌即可！这个令牌无需具备操作B网站所有资源的权限，也无需永久有效，只要满足A网站打印照片需求即可。这么听来，是不是有点像单点登录？NONONO！千万不要混淆概念！单点登录是用户一次登录，自己可以操作其他关联的服务资源。OAuth2则是用户给一个系统授权，可以直接操作其他系统资源的一种方式。但SpringSecurity的OAuth2也是可以实现单点登录的！
&emsp;&emsp;总结一句：SpringSecurity的OAuth2可以做服务之间资源共享，也可以实现单点登录！

## 3.OAuth2.0中四种授权方式
&emsp;&emsp;为了说明四种模式先准备一张图



### 3.1授权码模式（authorization code）
流程
说明：【A服务客户端】需要用到【B服务资源服务】中的资源
1. 【A服务客户端】将用户自动导航到【B服务认证服务】，这一步用户需要提供一个回调地址，以备【B服务认证服务】返回授权码使用。
2. 用户点击授权按钮表示让【A服务客户端】使用【B服务资源服务】，这一步需要用户登录B服务，也就是说用户要事先具有B服务的使用权限。
3. 【B服务认证服务】生成授权码，授权码将通过第一步提供的回调地址，返回给【A服务客户端】。
`注意`这个授权码并非通行【B服务资源服务】的通行凭证。
4. 【A服务认证服务】携带上一步得到的授权码向【B服务认证服务】发送请求，获取通行凭证token。
5. 【B服务认证服务】给【A服务认证服务】返回令牌token和更新令牌refresh token。

`使用场景`授权码模式是OAuth2中最安全最完善的一种模式，应用场景最广泛，可以实现服务之间的调用，常见的微信，QQ等第三方登录也可采用这种方式实现。
### 3.2简化模式（implicit）
流程
说明：简化模式中没有【A服务认证服务】这一部分，全部有【A服务客户端】与B服务交互，整个过程不再有授权码，token直接暴露在浏览器。
1. 【A服务客户端】将用户自动导航到【B服务认证服务】，这一步用户需要提供一个回调地址，以备【B服务认证服务】返回token使用，还会携带一个【A服务客户端】的状态标识state。
2. 用户点击授权按钮表示让【A服务客户端】使用【B服务资源服务】，这一步需要用户登录B服务，也就是说用户要事先具有B服务的使用权限。
3. 【B服务认证服务】生成通行令牌token，token将通过第一步提供的回调地址，返回给【A服务客户端】。

`使用场景`
适用于A服务没有服务器的情况。比如：纯手机小程序，JavaScript语言实现的网页插件等。
### 3.3密码模式（resource owner password credentials）
流程
1. 直接告诉【A服务客户端】自己的【B服务认证服务】的用户名和密码
2. 【A服务客户端】携带【B服务认证服务】的用户名和密码向【B服务认证服务】发起请求获取token。
3. 【B服务认证服务】给【A服务客户端】颁发token。

`使用场景`
此种模式虽然简单，但是用户将B服务的用户名和密码暴露给了A服务，需要两个服务信任度非常高才能使用。
### 3.4客户端模式（client credentials）
流程
说明：这种模式其实已经不太属于OAuth2的范畴了。A服务完全脱离用户，以自己的身份去向B服务索取token。换言之，用户无需具备B服务的使用权也可以。完全是A服务与B服务内部的交互，与用户无关了。
1. A服务向B服务索取token。
2. B服务返回token给A服务。

`使用场景`
A服务本身需要B服务资源，与用户无关。

## 4.OAuth2.0中表结构说明
`说明`
&emsp;&emsp;如果只是写个测试案例，完全可以不用连接数据库，直接将用户等信息写在项目中就行。
&emsp;&emsp;但是，我们应该把眼光放在企业开发中。试想，我们自己做的一个软件，想使用微信第三方登录。难道你还指望微信去修改他们的代码，让我们去访问？想都别想！那么微信会怎么做呢？微信会提供好一个接入的入口，让我们自己去申请访问权限。这些数据自然而然需要保存在数据库中！所以，我们将直接讲解数据库版实现方式！
`建表语句`
官方SQL地址：
https://github.com/spring-projects/spring-security-oauth/blob/master/spring-securityoauth2/src/test/resources/schema.sql

```sql
/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.16 : Database - security_authority
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `oauth_access_token` */

DROP TABLE IF EXISTS `oauth_access_token`;

CREATE TABLE `oauth_access_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` longblob,
  `authentication_id` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `authentication` longblob,
  `refresh_token` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_access_token` */

/*Table structure for table `oauth_approvals` */

DROP TABLE IF EXISTS `oauth_approvals`;

CREATE TABLE `oauth_approvals` (
  `userId` varchar(255) DEFAULT NULL,
  `clientId` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `expiresAt` datetime DEFAULT NULL,
  `lastModifiedAt` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_approvals` */

/*Table structure for table `oauth_client_details` */

DROP TABLE IF EXISTS `oauth_client_details`;

CREATE TABLE `oauth_client_details` (
  `client_id` varchar(255) NOT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  `client_secret` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `authorized_grant_types` varchar(255) DEFAULT NULL,
  `web_server_redirect_uri` varchar(255) DEFAULT NULL,
  `authorities` varchar(255) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(255) DEFAULT NULL,
  `autoapprove` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_client_details` */

/*Table structure for table `oauth_client_token` */

DROP TABLE IF EXISTS `oauth_client_token`;

CREATE TABLE `oauth_client_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` longblob,
  `authentication_id` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_client_token` */

/*Table structure for table `oauth_code` */

DROP TABLE IF EXISTS `oauth_code`;

CREATE TABLE `oauth_code` (
  `code` varchar(255) DEFAULT NULL,
  `authentication` varbinary(2550) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_code` */

/*Table structure for table `oauth_refresh_token` */

DROP TABLE IF EXISTS `oauth_refresh_token`;

CREATE TABLE `oauth_refresh_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` longblob,
  `authentication` longblob
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `oauth_refresh_token` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

```

## 5.表字段说明
### 5.1oauth_client_details【核心表】
|字段名 |	字段说明 |
|--|:--|
client_id 	|主键,必须唯一,不能为空. 用于唯一标识每一个客户端(client); 在注册时必须填写(也可由服务 端自动生成). 对于不同的grant_type,该字段都是必须的. 在实际应用中的另一个名称叫 appKey,与client_id是同一个概念.
resource_ids |	客户端所能访问的资源id集合,多个资源时用逗号(,)分隔,如: “unity-resource,mobile- resource”. 该字段的值必须来源于与security.xml中标签?oauth2:resource-server的属性 resource-id值一致. 在security.xml配置有几个?oauth2:resource-server标签, 则该字段可以 使用几个该值. 在实际应用中, 我们一般将资源进行分类,并分别配置对应 的?oauth2:resource-server,如订单资源配置一个?oauth2:resource-server, 用户资源又配置 一个?oauth2:resource-server. 当注册客户端时,根据实际需要可选择资源id,也可根据不同的 注册流程,赋予对应的资源id.
client_secret	|用于指定客户端(client)的访问密匙; 在注册时必须填写(也可由服务端自动生成). 对于不同的 grant_type,该字段都是必须的. 在实际应用中的另一个名称叫appSecret,与client_secret是 同一个概念.
scope 	|指定客户端申请的权限范围,可选值包括read,write,trust;若有多个权限范围用逗号(,)分隔,如: “read,write”. scope的值与security.xml中配置的?intercept-url的access属性有关系. 如?intercept-url的配置为?intercept-url pattern="/m/**" access=“ROLE_MOBILE,SCOPE_READ”/>则说明访问该URL时的客户端必须有read权限范 围. write的配置值为SCOPE_WRITE, trust的配置值为SCOPE_TRUST. 在实际应该中, 该值一 般由服务端指定, 常用的值为read,write.
authorized_grant_types|指定客户端支持的grant_type,可选值包括 authorization_code,password,refresh_token,implicit,client_credentials, 若支持多个 grant_type用逗号(,)分隔,如: “authorization_code,password”. 在实际应用中,当注册时,该字 段是一般由服务器端指定的,而不是由申请者去选择的,最常用的grant_type组合有: “authorization_code,refresh_token”(针对通过浏览器访问的客户端); “password,refresh_token”(针对移动设备的客户端). implicit与client_credentials在实际中 很少使用.
web_server_redirect_uri |客户端的重定向URI,可为空, 当grant_type为authorization_code或implicit时, 在Oauth的流 程中会使用并检查与注册时填写的redirect_uri是否一致. 下面分别说明:当 grant_type=authorization_code时, 第一步 从 spring-oauth-server获取 'code’时客户端发 起请求时必须有redirect_uri参数, 该参数的值必须与 web_server_redirect_uri的值一致. 第 二步 用 ‘code’ 换取 ‘access_token’ 时客户也必须传递相同的redirect_uri. 在实际应用中, web_server_redirect_uri在注册时是必须填写的, 一般用来处理服务器返回的code, 验证 state是否合法与通过code去换取access_token值.在spring-oauth-client项目中, 可具体参考 AuthorizationCodeController.java中的authorizationCodeCallback方法.当 grant_type=implicit时通过redirect_uri的hash值来传递access_token值. 如:http://localhost:7777/spring-oauth-client/implicit#access_token=dc891f4a-ac88- 4ba6-8224-a2497e013865&token_type=bearer&expires_in=43199然后客户端通过JS等从 hash值中取到access_token值.
authorities 	|指定客户端所拥有的Spring Security的权限值,可选, 若有多个权限值,用逗号(,)分隔, 如: "ROLE_
access_token_validity|设定客户端的access_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间 值(60 * 60 * 12, 12小时). 在服务端获取的access_token JSON数据中的expires_in字段的值 即为当前access_token的有效时间值. 在项目中, 可具体参考DefaultTokenServices.java中属 性accessTokenValiditySeconds. 在实际应用中, 该值一般是由服务端处理的, 不需要客户端 自定义.refresh_token_validity 设定客户端的refresh_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 24 * 30, 30天). 若客户端的grant_type不包 括refresh_token,则不用关心该字段 在项目中, 可具体参考DefaultTokenServices.java中属 性refreshTokenValiditySeconds. 在实际应用中, 该值一般是由服务端处理的, 不需要客户端 自定义.
additional_information |这是一个预留的字段,在Oauth的流程中没有实际的使用,可选,但若设置值,必须是JSON格式的 数据,如:{“country”:“CN”,“country_code”:“086”}按照spring-security-oauth项目中对该字段 的描述 Additional information for this client, not need by the vanilla OAuth protocol but might be useful, for example,for storing descriptive information. (详见 ClientDetails.java的getAdditionalInformation()方法的注释)在实际应用中, 可以用该字段来 存储关于客户端的一些其他信息,如客户端的国家,地区,注册时的IP地址等等.create_time 数据的创建时间,精确到秒,由数据库在插入数据时取当前系统时间自动生成(扩展字段)
archived |用于标识客户端是否已存档(即实现逻辑删除),默认值为’0’(即未存档). 对该字段的具体使用请 参考CustomJdbcClientDetailsService.java,在该类中,扩展了在查询client_details的SQL加上 archived = 0条件 (扩展字段)
trusted|	设置客户端是否为受信任的,默认为’0’(即不受信任的,1为受信任的). 该字段只适用于 grant_type="authorization_code"的情况,当用户登录成功后,若该值为0,则会跳转到让用户 Approve的页面让用户同意授权, 若该字段为1,则在登录后不需要再让用户Approve同意授权 (因为是受信任的). 对该字段的具体使用请参考OauthUserApprovalHandler.java. (扩展字 段)
autoapprove	|设置用户是否自动Approval操作, 默认值为 ‘false’, 可选值包括 ‘true’,‘false’, ‘read’,‘write’. 该 字段只适用于grant_type="authorization_code"的情况,当用户登录成功后,若该值为’true’或 支持的scope值,则会跳过用户Approve的页面, 直接授权. 该字段与 trusted 有类似的功能, 是 spring-security-oauth2 的 2.0 版本后添加的新属性. 在项目中,主要操作 oauth_client_details表的类是JdbcClientDetailsService.java, 更多的细节请参考该类. 也可 以根据实际的需要,去扩展或修改该类的实现.
	
	
### 5.2oauth_client_token
|字段名 |	字段说明 |
|--|:--|
create_time |数据的创建时间,精确到秒,由数据库在插入数据时取当前系统时间自动生成(扩展字段)
token_id|从服务器端获取到的access_token的值.
token |这是一个二进制的字段, 存储的数据是OAuth2AccessToken.java对象序列化后的二进制数据.
authentication_id|该字段具有唯一性, 是根据当前的username(如果有),client_id与scope通过MD5加密生成的. 具体实现请参考DefaultClientKeyGenerator.java类.
user_name|登录时的用户名
client_id |

>该表用于在客户端系统中存储从服务端获取的token数据, 在spring-oauth-server项目中未使用到. 对oauth_client_token表的主要操作在JdbcClientTokenServices.java类中, 更多的细节请参考该类.

### 5.3oauth_access_token
|字段名 |	字段说明 |
|--|:--|
create_time |数据的创建时间,精确到秒,由数据库在插入数据时取当前系统时间自动生成(扩展字段)
token_id|从服务器端获取到的access_token的值.
token |这是一个二进制的字段, 存储的数据是OAuth2AccessToken.java对象序列化后的二进制数据.
authentication_id|该字段具有唯一性, 是根据当前的username(如果有),client_id与scope通过MD5加密生成的. 具体实现请参考DefaultClientKeyGenerator.java类.
user_name|登录时的用户名
client_id  |
authentication|存储将OAuth2Authentication.java对象序列化后的二进制数据.
refresh_token|该字段的值是将refresh_token的值通过MD5加密后存储的. 在项目中,主要操作oauth_access_token表的对象是JdbcTokenStore.java. 更多的细节请参考该类
		
### 5.4oauth_refresh_token
|字段名 |	字段说明 |
|--|:--|
create_time |数据的创建时间,精确到秒,由数据库在插入数据时取当前系统时间自动生成(扩展字段)
token_id	|该字段的值是将refresh_token的值通过MD5加密后存储的.
token|存储将OAuth2RefreshToken.java对象序列化后的二进制数据
authentication	|存储将OAuth2RefreshToken.java对象序列化后的二进制数据
### 5.5oauth_code

|字段名 |	字段说明 |
|--|:--|
create_time	|数据的创建时间,精确到秒,由数据库在插入数据时取当前系统时间自动生成(扩展字段)
code	|存储服务端系统生成的code的值(未加密).
authentication	|存储将AuthorizationRequestHolder.java对象序列化后的二进制数据.

# 二、OAuth2.0实战案例
&emsp;&emsp;本案例同样通过maven的聚合工程来实现。
## 1.创建父工程
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212223459180.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
设置pom文件

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.3.RELEASE</version>
    <relativePath/>
</parent>

<properties>
    <spring-cloud.version>Greenwich.RELEASE</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<repositories>
    <repository>
        <id>spring-snapshots</id>
        <name>Spring Snapshots</name>
        <url>https://repo.spring.io/snapshot</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

## 2.创建资源项目
&emsp;&emsp;接下来创建我们的资源项目
### 2.1创建项目
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121222355833.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
### 2.2导入依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.springframework.security.oauth.boot/spring-security-oauth2-autoconfigure -->
    <dependency>
        <groupId>org.springframework.security.oauth.boot</groupId>
        <artifactId>spring-security-oauth2-autoconfigure</artifactId>
        <version>2.1.0.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
        <version>2.1.0.RELEASE</version>
        <exclusions>
            <exclusion>
                <artifactId>org.springframework.security.oauth.boot</artifactId>
                <groupId>spring-security-oauth2-autoconfigure</groupId>
            </exclusion>
        </exclusions>
    </dependency>


    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.47</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.10</version>
    </dependency>
</dependencies>

```

### 2.3配置文件

```xml
server:
  port: 9002
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/srm
    username: root
    password: 123456
    type: com.alibaba.druid.pool.DruidDataSource
  main:
    allow-bean-definition-overriding: true #允许我们自己覆盖spring放入到IOC容器的对象
mybatis:
  type-aliases-package: com.dpb.domain
  mapper-locations: classpath:mapper/*.xml
logging:
  level:
    com.dpb: debug
```

### 2.4启动类

```java
package com.dpb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 22:33
 */
@SpringBootApplication
@MapperScan("com.dpb.mapper")
public class OAuthSourceApp {

    public static void main(String[] args) {
        SpringApplication.run(OAuthSourceApp.class,args);
    }
}
```

### 2.5控制器

```java
/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 22:34
 */
@RestController
public class ProductController {

    @RequestMapping("/findAll")
    public String findAll(){
        return "产品列表信息...";
    }
}
```

&emsp;&emsp;因为我们引入了 SpringSecurity，所以我们此时没法直接方法 findAll方法，启动服务后访问如下:
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212223753481.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

那么如何解决呢？前面我们是采用单点登录的方式解决了这个问题，那么今天我们把这个资源交给`OAuth2`来管理，使用通行的token来访问资源

### 2.6将访问资源作为OAuth2的资源来管理
复制前面介绍的JWT中的相关代码（GitHub地址会提供）
即便是用OAuth2管理资源，也一样需要认证，这两个对象还是需要的。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212223905455.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
### 2.7编写资源管理配置类

```java
package com.dpb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 22:47
 */
@Configuration
@EnableResourceServer
public class OAuthSourceConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    /**
     * 指定token的持久化策略
     * InMemoryTokenStore表示将token存储在内存
     * Redis表示将token存储在redis中
     * JdbcTokenStore存储在数据库中
     * @return
     */
    @Bean
    public TokenStore jdbcTokenStore(){
        return new JdbcTokenStore(dataSource);
    }

    /**
     * 指定当前资源的id和存储方案
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("product_api").tokenStore(jdbcTokenStore());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //指定不同请求方式访问资源所需要的权限，一般查询是read，其余是write。
                .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('read')")
                .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.PATCH, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('write')")
                .and()
                .headers().addHeaderWriter((request, response) -> {
            response.addHeader("Access-Control-Allow-Origin", "*");//允许跨域
            if (request.getMethod().equals("OPTIONS")) {//如果是跨域的预检请求，则原封不动向下传达请求头信息
                response.setHeader("Access-Control-Allow-Methods", request.getHeader("Access-Control-Request-Method"));
                response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
            }
        });
    }
}
```

## 3.创建认证项目
&emsp;&emsp;接下来我们创建认证相关的项目
### 3.1创建项目
![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121222400979.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

### 3.2导入依赖
和source项目的一样

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.springframework.security.oauth.boot/spring-security-oauth2-autoconfigure -->
    <dependency>
        <groupId>org.springframework.security.oauth.boot</groupId>
        <artifactId>spring-security-oauth2-autoconfigure</artifactId>
        <version>2.1.0.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
        <version>2.1.0.RELEASE</version>
        <exclusions>
            <exclusion>
                <artifactId>org.springframework.security.oauth.boot</artifactId>
                <groupId>spring-security-oauth2-autoconfigure</groupId>
            </exclusion>
        </exclusions>
    </dependency>


    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.47</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.10</version>
    </dependency>
</dependencies>
```

### 3.3配置文件

```xml
server:
  port: 9001
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/srm
    username: root
    password: 123456
    type: com.alibaba.druid.pool.DruidDataSource
  main:
    allow-bean-definition-overriding: true #允许我们自己覆盖spring放入到IOC容器的对象
mybatis:
  type-aliases-package: com.dpb.domain
  mapper-locations: classpath:mapper/*.xml
  
logging:
  level:
    com.dpb: debug
```

### 3.4启动类

```java
/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 23:06
 */
@SpringBootApplication
@MapperScan("com.dpb.mapper")
public class OAuthServerApp {
    public static void main(String[] args) {
        SpringApplication.run(OAuthServerApp.class,args);
    }
}
```

### 3.5复制之前认证的代码
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224508844.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

### 3.6提供SpringSecurity的配置类

```java
package com.dpb.config;

import com.dpb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 23:09
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .permitAll()
                .and()
                .csrf()
                .disable();
    }

    //AuthenticationManager对象在OAuth2认证服务中要使用，提前放入IOC容器中
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

### 3.7提供OAuth2的配置类

```java
package com.dpb.config;

import com.dpb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * @program: springboot-security-oauth2-demo
 * @description:
 * @author: 波波烤鸭
 * @create: 2019-12-04 23:12
 */
@Configuration
@EnableAuthorizationServer
public class OauthServerConfig extends AuthorizationServerConfigurerAdapter {
    //数据库连接池对象
    @Autowired
    private DataSource dataSource;

    //认证业务对象
    @Autowired
    private UserService userService;

    //授权模式专用对象
    @Autowired
    private AuthenticationManager authenticationManager;

    //客户端信息来源
    @Bean
    public JdbcClientDetailsService jdbcClientDetailsService(){
        return new JdbcClientDetailsService(dataSource);
    }

    //token保存策略
    @Bean
    public TokenStore tokenStore(){
        return new JdbcTokenStore(dataSource);
    }

    //授权信息保存策略
    @Bean
    public ApprovalStore approvalStore(){
        return new JdbcApprovalStore(dataSource);
    }

    //授权码模式数据来源
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(){
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    //指定客户端信息的数据库来源
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(jdbcClientDetailsService());
    }

    //检查token的策略
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients();
        security.checkTokenAccess("isAuthenticated()");
    }

    //OAuth2的主配置信息
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .approvalStore(approvalStore())
                .authenticationManager(authenticationManager)
                .authorizationCodeServices(authorizationCodeServices())
                .tokenStore(tokenStore());
    }
}
```


## 4.测试
### 4.1在数据库中手动添加客户端信息
&emsp;&emsp;所有要使用当前项目资源的项目，都是我们的客户端。比如我们之前举的例子，A服务打印照片，B服务存储照片。A服务要使用B服务的资源，那么A服务就是B服务的客户端。这里要区分用户的信息和客户端信息，用户信息是用户在B服务上注册的用户信息，在sys_user表中。客户端信息是A服务在B服务中注册的账号，在OAuth2的oauth_client_details表中。
测试数据sql语句如下：

```sql
INSERT INTO `oauth_client_details` (
	`client_id`,
	`resource_ids`,
	`client_secret`,
	`scope`,
	`authorized_grant_types`,
	`web_server_redirect_uri`,
	`authorities`,
	`access_token_validity`,
	`refresh_token_validity`,
	`additional_information`,
	`autoapprove`
)
VALUES
	(
		'bobo_one',
		'product_api',
		'$2a$10$CYX9OMv0yO8wR8rE19N2fOaXDJondci5uR68k2eQJm50q8ESsDMlC',
		'read, write',
		'client_credentials,implicit,authorization_code,refresh_token,password',
		'http://www.baidu.com',
		NULL,
		NULL,
		NULL,
		NULL,
		'false'
	);
```

这里`注意`resource_ids不要写错，回调地址web_server_redirect_uri先写成百度。
启动两个服务测试

### 4.2 授权码模式测试
在地址栏访问地址
http://localhost:9001/oauth/authorize?response_type=code&client_id=bobo_one
跳转到SpringSecurity默认认证页面，提示用户登录个人账户【这里是sys_user表中的数据】
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224728896.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

登录成功后询问用户是否给予操作资源的权限，具体给什么权限。`Approve`是授权，`Deny`是拒绝。这里我们选择read和write都给予Approve
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224752335.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
点击Authorize后跳转到回调地址并获取授权码

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224805978.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224812498.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

使用授权码到服务器申请通行令牌token(测试使用的是PostMan)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224825647.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
重启资源服务器，然后携带通行令牌再次去访问资源服务器，大功告成！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224855190.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
### 4.3简化模式测试
在地址栏访问地址
http://localhost:9001/oauth/authorize?response_type=token&client_id=bobo_one

由于上面用户已经登录过了，所以无需再次登录，其实和上面是有登录步骤的，这时，浏览器直接返回了token
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224916702.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)
直接访问资源服务器

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224930157.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

### 4.4密码模式测试 
申请token

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212224947514.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

### 4.5客户端模式测试
申请token

![在这里插入图片描述](https://img-blog.csdnimg.cn/20191212225003804.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/2019121222501125.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9kcGItYm9ib2thb3lhLXNtLmJsb2cuY3Nkbi5uZXQ=,size_16,color_FFFFFF,t_70)

搞定~相关案例实现
