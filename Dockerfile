# 1단계: 빌드 환경
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

# Gradle 파일 복사 (캐시 최적화)
COPY hitmeup_backend/gradlew .
COPY hitmeup_backend/gradle gradle
COPY hitmeup_backend/build.gradle.kts .
COPY hitmeup_backend/settings.gradle.kts .
COPY hitmeup_backend/src src

# 의존성 다운로드 (캐시 레이어 분리)
RUN ./gradlew dependencies --no-daemon

# 2단계: 빌드 실행
RUN ./gradlew build -x test --no-daemon

# 3단계: 실행 환경
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 빌드 결과물만 복사 (수정된 부분)
COPY --from=builder /app/build/libs/hitmeup_backend-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수 및 포트 설정
ENV PORT=8080
EXPOSE 8080

# 4단계: 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]