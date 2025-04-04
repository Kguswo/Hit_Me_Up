package com.hitmeup.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*


@Configuration
class FirebaseConfig {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    @Throws(IOException::class)
    fun firebaseApp(): FirebaseApp {
        try {
            // 시스템 환경 변수 로깅
            logger.info("시스템 환경 변수 목록:")
            System.getenv().forEach { (key, value) ->
                if (!key.contains("secret", ignoreCase = true) &&
                    !key.contains("password", ignoreCase = true) &&
                    !key.contains("firebase", ignoreCase = true)
                ) {
                    logger.info("$key = $value")
                } else {
                    logger.info("$key = [보안 값]")
                }
            }

            // 이미 초기화된 앱 있는지 체크
            val existingApps = FirebaseApp.getApps()
            if (existingApps.isNotEmpty()) {
                logger.info("기존 Firebase 앱을 사용합니다.")
                return FirebaseApp.getInstance()
            }

            logger.info("Firebase 앱 초기화 시도 중...")

            // 환경 변수에서 Firebase 설정 가져오기 시도
            val firebaseConfigEnv = System.getenv("FIREBASE_CONFIG")

            if (firebaseConfigEnv != null) {
                logger.info("FIREBASE_CONFIG 환경 변수가 존재합니다.")
                try {
                    // 환경 변수의 내용이 유효한 JSON인지 간단히 확인
                    if (firebaseConfigEnv.trim().startsWith("{") && firebaseConfigEnv.trim().endsWith("}")) {
                        logger.info("Firebase 구성이 JSON 형식으로 보입니다.")
                    } else {
                        logger.warn("Firebase 구성이 JSON 형식이 아닐 수 있습니다.")
                    }

                    val credentials =
                        GoogleCredentials.fromStream(ByteArrayInputStream(firebaseConfigEnv.toByteArray()))
                    val options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build()

                    val app = FirebaseApp.initializeApp(options)
                    logger.info("Firebase 앱 초기화 성공: ${app.name}")
                    return app
                } catch (e: Exception) {
                    logger.error("환경 변수에서 Firebase 초기화 실패: ${e.message}", e)
                    // 환경 변수 방식이 실패하면 파일 방식으로 폴백
                    logger.info("파일 방식으로 초기화 시도")
                }
            } else {
                logger.warn("FIREBASE_CONFIG 환경 변수가 없습니다.")
            }

            // 로컬 파일에서 로드 (개발 환경용)
            logger.info("로컬 파일에서 Firebase 설정을 로드합니다.")
            val resource = ClassPathResource("firebase-service-account.json")
            if (!resource.exists()) {
                logger.error("Firebase 서비스 계정 파일을 찾을 수 없습니다: firebase-service-account.json")
                throw IOException("Firebase 서비스 계정 파일을 찾을 수 없습니다.")
            }

            val credentials = GoogleCredentials.fromStream(resource.inputStream)
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            val app = FirebaseApp.initializeApp(options)
            logger.info("Firebase 앱 초기화 성공: ${app.name}")
            return app
        } catch (e: Exception) {
            logger.error("Firebase 앱 초기화 중 오류 발생: ${e.message}", e)
            throw e
        }
    }
}