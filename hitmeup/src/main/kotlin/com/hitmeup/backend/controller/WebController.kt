package com.hitmeup.backend.controller

import com.hitmeup.backend.model.ApiResponse
import com.hitmeup.backend.service.FirestoreHitsService
import com.hitmeup.backend.util.toSuccessResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class WebController(private val hitsService: FirestoreHitsService) {

	@Value("\${app.domain}")
	private lateinit var domain: String

	@Value("\${app.frontend-url}")
	private lateinit var frontendUrl: String

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
		val encodedTitle = java.net.URLEncoder.encode(badgeForm.title, java.nio.charset.StandardCharsets.UTF_8)
		// 배지 생성시 db에 등록
		hitsService.ensureUrlExists(badgeForm.url)

		val badgeUrl = "${frontendUrl}/api/count/preview?url=${encodedUrl}&title=${encodedTitle}&title_bg=${badgeForm.titleBg.removePrefix("#")}&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

		val notionUrl = "${frontendUrl}/api/count/increment?url=${encodedUrl}&title=${encodedTitle}&title_bg=${badgeForm.titleBg.removePrefix("#")}&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

		val markdownCode = """
			[![Hits]($notionUrl)](${frontendUrl})
		""".trimIndent()

		val htmlCode = """
			<a href="${frontendUrl}">
			  <img src="$notionUrl" alt="${encodedTitle}" />
			</a>
		""".trimIndent()

		val badgeInfo = BadgeInfo(
			badgeUrl = badgeUrl,
			notionUrl = notionUrl,
			markdownCode = markdownCode,
			htmlCode = htmlCode
		)

		return badgeInfo.toSuccessResponse()
	}

	// 기존 폼 처리 엔드포인트
	@PostMapping("/view")
	fun generateView(@ModelAttribute badgeForm: BadgeForm, model: Model): String {
		val encodedUrl = java.net.URLEncoder.encode(badgeForm.url, java.nio.charset.StandardCharsets.UTF_8)
		val encodedTitle = java.net.URLEncoder.encode(badgeForm.title, java.nio.charset.StandardCharsets.UTF_8) // 추가

		// 배지 생성시 db에 등록
		hitsService.ensureUrlExists(badgeForm.url)

		val badgeUrl = "${frontendUrl}/api/count/preview?url=${encodedUrl}&title=${encodedTitle}&title_bg=${badgeForm.titleBg.removePrefix("#")}&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

		val notionUrl = "${frontendUrl}/api/count/increment?url=${encodedUrl}&title=${encodedTitle}&title_bg=${badgeForm.titleBg.removePrefix("#")}&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

		val markdownCode = """
			[![Hits]($notionUrl)](${frontendUrl})
		""".trimIndent()

		val htmlCode = """
			<a href="${frontendUrl}">
			  <img src="$notionUrl" alt="${encodedTitle}" />
			</a>
		""".trimIndent()

		model.addAttribute("badgeForm", badgeForm)
		model.addAttribute("markdownCode", markdownCode)
		model.addAttribute("htmlCode", htmlCode)
		model.addAttribute("badgeUrl", badgeUrl)
		model.addAttribute("notionUrl", notionUrl)
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
		val notionUrl: String,
		val markdownCode: String,
		val htmlCode: String
	)
}