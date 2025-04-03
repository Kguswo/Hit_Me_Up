package com.hitmeup.backend.service

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutionException

@Service
class FirestoreHitsService(@Autowired private val firebaseApp: FirebaseApp) {

    private val logger = LoggerFactory.getLogger(FirestoreHitsService::class.java)
    private val firestore: Firestore = FirestoreClient.getFirestore(firebaseApp)
    private val collection = "hits"

    init {
        logger.info("FirestoreHitsService 초기화 중...")
//        firestore = FirestoreClient.getFirestore(firebaseApp)
        logger.info("Firestore 인스턴스 생성 성공")
    }

    // URL에 대한 방문자 수 증가
    fun incrementHits(url: String): Long {
        val documentRef = firestore.collection(collection).document(url)

        try {
            // 트랜잭션 내에서 카운터 증가
            return firestore.runTransaction { transaction ->
                val document = transaction.get(documentRef).get()
                val currentCount = if (document.exists()) {
                    (document.getLong("count") ?: 0)
                } else {
                    0
                }
                val newCount = currentCount + 1

                // 새 데이터로 문서 업데이트
                transaction.set(
                    documentRef,
                    mapOf(
                        "url" to url,
                        "count" to newCount,
                        "lastUpdated" to com.google.cloud.Timestamp.now()
                    )
                )

                newCount
            }.get()
        } catch (e: InterruptedException) {
            logger.error("방문자 수 증가 중 인터럽트 발생", e)
            Thread.currentThread().interrupt()
            return 1 // 기본값 반환
        } catch (e: ExecutionException) {
            logger.error("방문자 수 증가 중 실행 오류 발생", e)
            return 1 // 기본값 반환
        } catch (e: Exception) {
            logger.error("방문자 수 증가 중 일반 오류 발생", e)
            return 1 // 기본값 반환
        }
    }

    // URL에 대한 현재 방문자 수 조회
    fun getHits(url: String): Long {
        val documentRef = firestore.collection(collection).document(url)

        try {
            val document = documentRef.get().get()
            return if (document.exists()) {
                document.getLong("count") ?: 0
            } else {
                0
            }
        } catch (e: InterruptedException) {
            logger.error("방문자 수 조회 중 인터럽트 발생", e)
            Thread.currentThread().interrupt()
            return 0 // 기본값 반환
        } catch (e: ExecutionException) {
            logger.error("방문자 수 조회 중 실행 오류 발생", e)
            return 0 // 기본값 반환
        } catch (e: Exception) {
            logger.error("방문자 수 조회 중 일반 오류 발생", e)
            return 0 // 기본값 반환
        }
    }
}