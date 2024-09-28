FROM openjdk:17-jdk-slim

WORKDIR /app

COPY /build/libs/*.jar /app/haengdong-0.0.1-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-Duser.timezone=Asia/Seoul",  "-jar", "spring-s3-upload-0.0.1-SNAPSHOT.jar"]
