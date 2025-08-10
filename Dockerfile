# 使用指定版本的openjdk镜像
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制项目中的Maven压缩包并安装
COPY apache-maven-3.9.11-bin.tar.gz /tmp/apache-maven-3.9.11-bin.tar.gz
RUN tar -xzf /tmp/apache-maven-3.9.11-bin.tar.gz -C /opt/ && \
    mv /opt/apache-maven-3.9.11 /opt/maven && \
    ln -s /opt/maven/bin/mvn /usr/local/bin/mvn && \
    rm /tmp/apache-maven-3.9.11-bin.tar.gz

# 配置Maven使用阿里云镜像源
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyunmaven</id><mirrorOf>*</mirrorOf><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 配置Maven使用阿里云镜像源
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyunmaven</id><mirrorOf>*</mirrorOf><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# Copy project files
COPY . .

# 构建项目
RUN mvn clean package -DskipTests

# 运行应用
CMD ["java", "-jar", "target/*.jar"]