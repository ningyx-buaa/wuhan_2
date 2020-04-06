ring-for-cncert
===============

此项目为在Ring的基础上提供给中心的业务系统。

前端 / frontend
---------------

前端代码位于[frontend](./frontend)目录，基于Vue.js开发。

### 开发

+ 基础依赖：

[Node.js](https://nodejs.org/en/)，[cnpm](https://github.com/cnpm/cnpm)

+ 项目依赖安装：

~~~
cd frontend
cnpm install --save-dev
~~~

+ 本地运行

    - 本地仅调试前端：

        cnpm run dev-remote

    - 本地前端和后端联调

        cnpm run dev-local

+ 线上部署

SSH登录bd11，运行

~~~shell
cd /var/app/ring-for-cncert
git pull
cd frontend
cnpm run build
~~~

+ 注意事项

添加、删除依赖时，需要重新安装安装依赖：

~~~shell
cnpm install
cnpm run build
~~~

后端 / ring-cncert
-----------------

后端代码基于 spring-web 开发，使用 Maven 做项目依赖管理，推荐适应 [IntelliJ IDEA](https://www.jetbrains.com/idea/)
作为开发工具。

+ 项目结构：

    - data：ES，Hbase查询、存储
    - foundation：NLP相关，各类接口、各种度的计算
    - restful：提供给前端的restful API
    - task：离线计算任务，需要每天更新的指数、统计等

+ 项目导入：

IntelliJ IDEA -> File/Open -> 选择`ring-cncert`目录 -> Import Project from external model -> Maven -> Next

+ 本地运行和调试

运行 restful/test 目录下的 `act.ring.cncert.demo.DemoServer` 类中的 `main` 函数，在Idea右上角
中的Edit Configurations里将Working Directory设置为`$MODULE_DIR$`。

后端代码本地运行时使用`8080`端口。

+ 线上部署

SSH登录bd11，运行

~~~shell
cd /var/app/ring-for-cncert
git pull
cd ring-cncert
mvn compile package install
~~~

重启后端Restful服务：

~~~
/var/app/ring-for-cncert/ring-cncert/tools/run-restful-server.sh restart
~~~

+ 注意事项

请务必将[ring-cncert](./ring-cncert)目录下的`intellij-java-google-style.xml`文件设置为Idea的代码格式化规范，
设置方式：File -> Settings -> Editor -> Code Style -> Scheme右侧的下拉菜单中选择 Import Scheme，选择文件导入。

**每次提交之前，请务必在Idea左侧的项目根目录`ring-cncert`上单机鼠标右键，执行“Reformat Code”。**

关于
----

+ 线上演示网址：

1. 内网：[http://10.1.1.11:8082/](http://10.1.1.11:8082/)
2. 外网：[ring.act.buaa.edu.cn/cc/index.html](ring.act.buaa.edu.cn/cc/index.html)
