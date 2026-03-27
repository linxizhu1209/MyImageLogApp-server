FROM gradle:8.7-jdk17 AS build
WORKDIR /workspace

# 캐시 효율을 위해 의존성 관련 파일을 먼저 복사
COPY gradle /workspace/gradle
COPY gradlew /workspace/gradlew
COPY build.gradle /workspace/build.gradle
COPY settings.gradle /workspace/settings.gradle
# gradle.properties는 없을 수도 있음

RUN chmod +x /workspace/gradlew && /workspace/gradlew --no-daemon dependencies || true

# 실제 소스 복사 후 빌드
COPY . /workspace
RUN /workspace/gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

