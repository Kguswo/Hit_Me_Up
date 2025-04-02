package com.hitmeup.backend.controller

import com.hitmeup.backend.model.ApiResponse
import com.hitmeup.backend.service.HitsService
import com.hitmeup.backend.util.SvgGenerator
import com.hitmeup.backend.util.toSuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api")
class HitsController(private val hitsService: HitsService) {

    @GetMapping("/count/increment", produces = ["image/svg+xml"])
    fun getBadge(
        @RequestParam("url") encodedUrl: String,
        @RequestParam("count_bg", defaultValue = "#79C83D") countBg: String,
        @RequestParam("title_bg", defaultValue = "#555555") titleBg: String,
        @RequestParam("title", defaultValue = "hits") title: String,
        @RequestParam("edge_flat", defaultValue = "false") edgeFlat: Boolean
    ): ResponseEntity<String> {
        val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)
        val count = hitsService.incrementHits(url)

        val svg = SvgGenerator.generateSvg(title, titleBg, countBg, edgeFlat, count)

        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .body(svg)
    }

    @GetMapping("/count")
    fun getCount(@RequestParam("url") encodedUrl: String): ResponseEntity<ApiResponse<CountResponse>> {
        val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8)
        val count = hitsService.getHits(url)

        return CountResponse(url, count).toSuccessResponse()
    }

    data class CountResponse(val url: String, val count: Long)
}