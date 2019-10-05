## 多线程爬虫和ES数据分析

### 1、项目目标
- 抓取网站数据
- 改造多线程爬虫
- 使用ES数据分析
- 实现一个搜索引擎


### 2、项目技术栈

- Java、Maven、Mybatis、Junit5
- HttpClient、Jsoup解析Html
- 代码检测插件 maven-checkstyle-plugin
- 自动bug检查插件 SpotBug bug
- 持续集成，CircleCI
- Docker安装Mysql
- 使用Mybatis做ORM关系映射
- 代码重构：方法抽离、DAO（JDBC连接、Mybatis连接）

### 3、确定算法

- 从一个节点，遍历所有的节点
- 算法：广度优先算法的一个变体
    - DFS深度优先
    - BfS广度优先 

### 4、注意点

- 多线程：先取值，根据值再判断决定做一件事，在多线程中会比较危险，因为两个线程可能同时执行一句代码。解决方案：变成同步，使用synchronized。所有线程共享一个DAO。每个DAO在调用含有synchronized方法之前需要竞争获得一个锁，其他没拿到就得等待。


### 5、项目启动命令

- 启动数据库
```$xslt
docker run --name mysql -v `pwd`/mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=crawler -p 3306:3306 -d mysql
```

- 初始化数据库建表
```$xslt
mvn flyway:clean
mvn flyway:migrate 
```

- 运行启动
    



