# 1단계: 빌드 환경
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app

# Gradle 파일 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# 의존성 다운로드 (캐시 레이어 분리)
RUN ./gradlew dependencies --no-daemon

# 2단계: 빌드 실행
RUN ./gradlew build -x test --no-daemon

# 3단계: 실행 환경
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 빌드 결과물만 복사 (수정된 부분)
COPY --from=builder /app/build/libs/hitmeup_backend-0.0.1-SNAPSHOT.jar app.jar

# 4단계: 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]