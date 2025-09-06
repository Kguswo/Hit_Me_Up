package com.hitmeup.backend.controller

import com.hitmeup.backend.model.ApiResponse
import com.hitmeup.backend.service.FirestoreHitsService
import com.hitmeup.backend.util.SvgGenerator
import com.hitmeup.backend.util.toSuccessResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api")
class HitsController(private val hitsService: FirestoreHitsService) {
    private val logger = LoggerFactory.getLogger(HitsController::class.java)
    private val restTemplate = RestTemplate()
    private val apiSecret = System.getenv("FIREBASE_ANALYTICS_SECRET")

    /**
     * 워밍업용 핑 엔드포인트
     */
    @GetMapping("/ping")
    fun ping(): String = "pong"

    /**
     * 실제 배지 생성 + 카운트 증가
     */
    @GetMapping("/count/increment", produces = ["image/svg+xml"])
    fun getBadge(
        @RequestParam("url") encodedUrl: String,
        @RequestParam("count_bg", defaultValue = "#79C83D") countBg: String,
        @RequestParam("title_bg", defaultValue = "#555555") titleBg: String,
        @RequestParam("title", defaultValue = "hits") title: String,
        @RequestParam("edge_flat", defaultValue = "false") edgeFlat: Boolean,
        @RequestParam("preview", defaultValue = "false") preview: Boolean,
    ): ResponseEntity<String> {
        val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)

        // preview 모드일땐 카운트 증가 안함. 현재 카운트 조회만 함.
        val count = if (preview) {
            hitsService.getHits(url)
        } else {
            hitsService.incrementHits(url).also {
                sendBadgeViewAnalytics()
            }
        }
        // # 접두사 확인 및 추가
        val formattedTitleBg = if (titleBg.startsWith("#")) titleBg else "#$titleBg"
        val formattedCountBg = if (countBg.startsWith("#")) countBg else "#$countBg"

        val svg = SvgGenerator.generateSvg(title, formattedTitleBg, formattedCountBg, edgeFlat, count)

        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .body(svg)
    }

    private fun sendBadgeViewAnalytics() {
        try {
            if (apiSecret.isNullOrBlank()) {
                logger.warn("Firebase Analytics API Secret이 설정되지 않음")
                return
            }

            val measurementId = "G-WLP9FYY9ER"
            val analyticsUrl = "https://www.google-analytics.com/mp/collect?measurement_id=$measurementId&api_secret=$apiSecret"

            val payload = mapOf(
                "client_id" to "server_${System.currentTimeMillis()}",
                "events" to listOf(
                    mapOf(
                        "name" to "badge_viewed",
                        "parameters" to mapOf(
                            "source" to "server"
                        )
                    )
                )
            )

            // HttpHeaders 명시적 설정
            val headers = org.springframework.http.HttpHeaders()
            headers.contentType = org.springframework.http.MediaType.APPLICATION_JSON

            val entity = org.springframework.http.HttpEntity(payload, headers)

            restTemplate.postForEntity(analyticsUrl, entity, String::class.java)
        } catch (e: Exception) {
            // 에러 무시
        }
    }

    /**
     * 미리보기 (항상 1)
     */
    @GetMapping("/count/preview", produces = ["image/svg+xml"])
    fun getPreviewBadge(
        @RequestParam("url") encodedUrl: String,
        @RequestParam("count_bg", defaultValue = "#79C83D") countBg: String,
        @RequestParam("title_bg", defaultValue = "#555555") titleBg: String,
        @RequestParam("title", defaultValue = "hits") title: String,
        @RequestParam("edge_flat", defaultValue = "false") edgeFlat: Boolean
    ): ResponseEntity<String> {
        // # 접두사 확인 및 추가
        val formattedTitleBg = if (titleBg.startsWith("#")) titleBg else "#$titleBg"
        val formattedCountBg = if (countBg.startsWith("#")) countBg else "#$countBg"

        // 항상 1로 고정된 카운트를 사용
        val svg = SvgGenerator.generateSvg(title, formattedTitleBg, formattedCountBg, edgeFlat, 1)

        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .body(svg)
    }

    /**
     * 현재 카운트 조회
     */
    @GetMapping("/count")
    fun getCount(@RequestParam("url") encodedUrl: String): ResponseEntity<ApiResponse<CountResponse>> {
        val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)
        val count = hitsService.getHits(url)

        return CountResponse(url, count).toSuccessResponse()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<String> {
        logger.error("API 호출 중 오류 발생", e)
        return ResponseEntity.status(500).body("서버 오류: ${e.message}")
    }

    data class CountResponse(val url: String, val count: Long)
}