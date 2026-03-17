FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src
RUN ./mvnw -DskipTests clean package
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/app-0.0.1-SNAPSHOT.jar"]