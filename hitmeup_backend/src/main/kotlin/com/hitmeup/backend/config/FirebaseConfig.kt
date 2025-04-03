package com.hitmeup.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FirebaseConfig {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    @Throws(IOException::class)
    fun firebaseApp(): FirebaseApp {
        try {
            // 이미 초기화된 앱 있는지 체크
            val existingApps = FirebaseApp.getApps()
            if (existingApps.isNotEmpty()) {
                logger.info("기존 Firebase 앱을 사용합니다.")
                return FirebaseApp.getInstance()
            }

            logger.info("Firebase 앱 초기화 시도 중...")
            val resource = ClassPathResource("firebase-service-account.json")
            if (!resource.exists()) {
                logger.error("Firebase 서비스 계정 파일을 찾을 수 없습니다: firebase-service-account.json")
                throw IOException("Firebase 서비스 계정 파일을 찾을 수 없습니다.")
            }

            logger.info("Firebase 서비스 계정 파일을 찾았습니다.")
            val serviceAccount = resource.inputStream

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
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