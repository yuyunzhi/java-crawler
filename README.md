## 多线程爬虫和ES数据分析

### 1、项目目标

- 添加代码检测插件 maven-checkstyle-plugin
- 使用httpclient [httpclient官网](http://hc.apache.org/httpcomponents-client-ga/quickstart.html)
- 配置CircleCI
- 爬取新浪新闻


### 2、项目技术栈
- CircleCI，持续集成
- Jsoup解析HTML
- 代码重构：方法抽离
- spotBug bug自动检查工具
- 使用Mybatis

### 3、确定算法

- 从一个节点，遍历所有的节点
- 算法：广度优先算法的一个变体
    - DFS深度优先
    - BfS广度优先 广度优先算法/队列数据结构/JDK队列实现
    
    



