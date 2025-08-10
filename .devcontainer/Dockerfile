FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# 安装项目中的Maven压缩包
COPY apache-maven-3.9.11-bin.tar.gz /tmp/apache-maven-3.9.11-bin.tar.gz

# 解压并设置Maven环境
RUN tar -xzf /tmp/apache-maven-3.9.11-bin.tar.gz -C /usr/local/ \
    && ln -s /usr/local/apache-maven-3.9.11/bin/mvn /usr/local/bin/mvn \
    && rm /tmp/apache-maven-3.9.11-bin.tar.gz

# 设置Maven环境变量
ENV MAVEN_HOME=/usr/local/apache-maven-3.9.11
ENV PATH=$PATH:$MAVEN_HOME/bin

# 验证Maven安装
RUN mvn --version

# 配置Maven使用阿里云镜像源加速依赖下载
RUN mkdir -p $MAVEN_HOME/conf \
    && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 \
                          https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
      <mirrors> \
        <mirror> \
          <id>aliyunmaven</id> \
          <mirrorOf>central</mirrorOf> \
          <name>阿里云公共仓库</name> \
          <url>https://maven.aliyun.com/repository/central</url> \
        </mirror> \
      </mirrors> \
    </settings>' > $MAVEN_HOME/conf/settings.xml

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1