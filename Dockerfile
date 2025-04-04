# 1단계: 빌드 환경
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

# Gradle 파일 복사 (캐시 최적화)
COPY hitmeup_backend/gradlew /app/
COPY hitmeup_backend/gradle /app/gradle
COPY hitmeup_backend/build.gradle.kts /app/
COPY hitmeup_backend/settings.gradle.kts /app/
COPY hitmeup_backend/src /app/src

# 권한 설정
RUN chmod +x /app/gradlew

# 2단계: 빌드 실행
RUN ./gradlew build -x test --no-daemon

# 3단계: 실행 환경
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 빌드 결과물만 복사
COPY --from=builder /app/build/libs/hitmeup_backend-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수 및 포트 설정
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
# 디버깅을 위한 추가 환경 변수
ENV JAVA_TOOL_OPTIONS="-XX:+PrintCommandLineFlags -verbose:class"

EXPOSE 8080

# 4단계: 실행 명령
ENTRYPOINT ["java", "-jar", "-Dserver.port=8080", "app.jar"]