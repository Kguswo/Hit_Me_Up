package com.hitmeup.backend.service

import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
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

    // URL 안전한 ID로 변환
    fun getDocumentIdFromUrl(url: String): String? {
        // Base64인코딩
        return Base64.getEncoder().encodeToString(url.toByteArray())
            .replace("/", "_")
            .replace("+", "-")
            .replace("=", "")
    }

    // URL DB에 존재하는지 확인, 없으면 추가
    fun ensureUrlExists(url: String) {
        val docId = getDocumentIdFromUrl(url)!!
        val documentRef = firestore.collection(collection).document(docId)

        try {
            val document = documentRef.get().get()
            if (!document.exists()) {
                documentRef.set(
                    mapOf(
                        "url" to url,
                        "count" to 0L,
                        "lastUpdated" to com.google.cloud.Timestamp.now(),
                    )
                ).get()
                logger.info("URL '$url'에 대한 새 문서 초기값 0으로 생성")
            } else {
                logger.info("URL '$url'에 대한 문서 이미 존재함")
            }
        } catch (e: Exception) {
            logger.error("URL 확인 중 오류 발생: ${e.message}", e)
        }
    }

    // URL에 대한 방문자 수 증가
    fun incrementHits(url: String): Long {
        val docId = getDocumentIdFromUrl(url)!!
        val documentRef = firestore.collection(collection).document(docId)

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
        val docId = getDocumentIdFromUrl(url)!!
        val documentRef = firestore.collection(collection).document(docId)

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