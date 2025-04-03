package com.hitmeup.backend.controller

import com.hitmeup.backend.model.ApiResponse
import com.hitmeup.backend.service.FirestoreHitsService
import com.hitmeup.backend.util.toSuccessResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class WebController(private val hitsService: FirestoreHitsService) {

    @Value("\${app.domain}")
    private lateinit var domain: String

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("badgeForm", BadgeForm())
        return "index"
    }

    // REST API 엔드포인트 - 배지 정보를 JSON으로 반환
    @PostMapping("/api/badge")
    @ResponseBody
    fun generateBadge(@RequestBody badgeForm: BadgeForm): ResponseEntity<ApiResponse<BadgeInfo>> {
        val encodedUrl = java.net.URLEncoder.encode(badgeForm.url, java.nio.charset.StandardCharsets.UTF_8)

        // 배지 생성시 db에 등록
        hitsService.ensureUrlExists(badgeForm.url)

        val markdownCode = """
            [![Hits](${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat})](${domain})
        """.trimIndent()

        val htmlCode = """
            <a href="${domain}">
              <img src="${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}" alt="${badgeForm.title}" />
            </a>
        """.trimIndent()

        val badgeUrl = "${domain}/api/count/preview?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix("#")
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

        val badgeInfo = BadgeInfo(
            badgeUrl = badgeUrl,
            markdownCode = markdownCode,
            htmlCode = htmlCode
        )

        return badgeInfo.toSuccessResponse()
    }

    // 기존 폼 처리 엔드포인트
    @PostMapping("/view")
    fun generateView(@ModelAttribute badgeForm: BadgeForm, model: Model): String {
        val encodedUrl = java.net.URLEncoder.encode(badgeForm.url, java.nio.charset.StandardCharsets.UTF_8)

        // 배지 생성시 db에 등록
        hitsService.ensureUrlExists(badgeForm.url)
        
        val markdownCode = """
            [![Hits](${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat})](${domain})
        """.trimIndent()

        val htmlCode = """
            <a href="${domain}">
              <img src="${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}" alt="${badgeForm.title}" />
            </a>
        """.trimIndent()

        val badgeUrl = "${domain}/api/count/preview?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix("#")
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

        model.addAttribute("badgeForm", badgeForm)
        model.addAttribute("markdownCode", markdownCode)
        model.addAttribute("htmlCode", htmlCode)
        model.addAttribute("badgeUrl", badgeUrl)
        model.addAttribute("showResult", true)

        return "index"
    }

    data class BadgeForm(
        var url: String = "",
        var title: String = "hits",
        var titleBg: String = "#555555",
        var countBg: String = "#79C83D",
        var edgeFlat: Boolean = false
    )

    data class BadgeInfo(
        val badgeUrl: String,
        val markdownCode: String,
        val htmlCode: String
    )
}