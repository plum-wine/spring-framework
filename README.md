## spring-framework 核心模块源码阅读

### 步骤一
使用Idea打开根目录的build.gradle文件

### 步骤二
根目录执行 ./gradlew :spring-oxm:compileTestJava  先对 Spring-oxm 模块进行预编译
根目录执行 ./gradlew build -x test  编译，整个Spring的源码。 后面的 -x test  是编译期间忽略测试用例
选中  spring-aspects  项目 右键，选择“Load/Unload Modules” 在弹出的窗体中进行设置

### 步骤三
新建名为"spring-debug"的gradle项目, 开始调试