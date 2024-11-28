FROM openjdk:17
ARG JAR_FILE=build/libs/dulian*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "-Djasypt.encryptor.password=${JASYPT_PASSWORD}", "/app.jar"]
