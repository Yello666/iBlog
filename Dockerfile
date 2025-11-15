# Step 1: 选择基础镜像
FROM eclipse-temurin:21-jdk

# Step 2: 设置工作目录
WORKDIR /app

# Step 3: 复制本地 Jar 包到镜像中
COPY iBlog-0.0.1-SNAPSHOT.jar app.jar

# Step 4: 暴露应用端口（Spring Boot 默认8080）
EXPOSE 8080

# Step 5: 指定容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
