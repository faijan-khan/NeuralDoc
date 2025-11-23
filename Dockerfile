# 1. Use a base image with Java 17
FROM eclipse-temurin:17-jdk-alpine

# 2. Set the working directory inside the box
WORKDIR /app

# 3. Copy your built app (JAR file) into the box
# (Note: You must run 'mvn clean package' first on your machine to create this jar)
COPY target/*.jar app.jar

# 4. Create a folder for your database
RUN mkdir -p data

# 5. Open port 8080
EXPOSE 8080

# 6. Run the app!
ENTRYPOINT ["java", "-jar", "app.jar"]