FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "target/poo-0.0.1-SNAPSHOT.jar"]
