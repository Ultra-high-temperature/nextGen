FROM openjdk:17-jdk-slim

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Extract the JAR file name and create a symbolic link
RUN ln -s target/nextGen-*.jar app.jar

EXPOSE 8899

ENTRYPOINT ["java", "-jar", "app.jar"]