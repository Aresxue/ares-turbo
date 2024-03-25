# Spring Boot 加速器项目

该项目旨在加速Spring Boot应用程序的启动过程，通过自定义ClassLoader来优化应用程序资源的加载。通过这种方式，可以减少启动时间并提高性能。

## 如何工作

Spring Boot应用程序通常使用`org.springframework.boot.loader.LaunchedURLClassLoader`来加载应用程序的类和资源。这个ClassLoader会在启动时加载许多类和资源，导致启动时间较长。本项目的目标是通过自定义ClassLoader来替代原有的ClassLoader，在初始化过程中，将所有的ClassLoader进行初始化，并生成一个索引，以便在后续的`findResources`操作中，不再需要从头遍历所有ClassLoader，而是可以直接从索引中获取需要遍历的ClassLoader数组。

## 使用方法

要在您的Spring Boot项目中使用这个加速器，您可以按照以下步骤操作：

### 1.改造插件原有的spring-boot-maven-plugin

将其改造为如下形式

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring-boot.version}</version>
    <executions>
      <execution>
        <goals>
          <goal>repackage</goal>
        </goals>
        <configuration>
          <layoutFactory implementation="cn.ares.turbo.loader.tools.AresTurboLayoutFactory"/>
        </configuration>
      </execution>
    </executions>
    <dependencies>
      <dependency>
        <groupId>io.github.aresxue</groupId>
        <artifactId>ares-turbo-loader-tool</artifactId>
        <version>2.2.2</version>
      </dependency>
    </dependencies>
 </plugin>
```
建议将其维护在自定义的parent中，无需业务感知。

### 2.添加jvm参数

添加如下jvm参数

```shell
-Dares.turbo.classloader.enable=true
```

## 贡献

如果您想为这个项目做出贡献，可以按照以下步骤操作：

Fork这个项目到您自己的GitHub仓库。

在您的本地计算机上克隆您的仓库。

创建一个新的分支，以进行您的更改。

在新分支上进行更改和改进。

提交更改并推送到您的GitHub仓库。

创建一个拉取请求（Pull Request），描述您的更改和改进。

我们将审查您的拉取请求，并将您的贡献合并到主项目中。

## 联系我们

如果您有任何问题或建议，请随时联系我们。您可以通过GitHub上的问题（Issues）功能提交问题或反馈。我们将尽力回复您的消息。

感谢您对这个项目的关注和支持！希望这个加速器可以帮助您加速Spring Boot应用程序的启动。