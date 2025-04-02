package com.hitmeup.backend.controller

import com.hitmeup.backend.service.HitsService
import com.hitmeup.backend.util.SvgGenerator
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HitsControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var hitsService: HitsService
    private lateinit var hitsController: HitsController

    @BeforeEach
    fun setUp() {
        hitsService = mockk()
        hitsController = HitsController(hitsService)
        mockMvc = MockMvcBuilders.standaloneSetup(hitsController).build()

        // 정적 메서드 모킹을 위한 설정
        mockkObject(SvgGenerator)
    }

    @AfterEach
    fun tearDown() {
        // 모킹 해제
        unmockkAll()
    }

    @Test
    fun `count API - 존재하는 URL의 카운트 조회`() {
        // given
        val url = "https://github.com/username"
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        every { hitsService.getHits(url) } returns 42L

        // when & then
        mockMvc.perform(get("/api/count?url=$encodedUrl"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.url").value(url))
            .andExpect(jsonPath("$.data.count").value(42))

        verify { hitsService.getHits(url) }
    }

    @Test
    fun `increment API - SVG 배지 생성 및 카운트 증가`() {
        // given
        val url = "https://github.com/username"
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        val title = "hits"
        val titleBg = "#555555"
        val countBg = "#79C83D"
        val edgeFlat = false
        val count = 42L
        val svgContent = "<svg>테스트 SVG</svg>"

        every { hitsService.incrementHits(url) } returns count
        every {
            SvgGenerator.generateSvg(title, titleBg, countBg, edgeFlat, count)
        } returns svgContent

        // when & then
        mockMvc.perform(get("/api/count/increment?url=$encodedUrl&title=$title&title_bg=$titleBg&count_bg=$countBg&edge_flat=$edgeFlat"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.valueOf("image/svg+xml;charset=UTF-8")))
            .andExpect(content().string(svgContent))
            .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))

        verify { hitsService.incrementHits(url) }
        verify { SvgGenerator.generateSvg(title, titleBg, countBg, edgeFlat, count) }
    }
}