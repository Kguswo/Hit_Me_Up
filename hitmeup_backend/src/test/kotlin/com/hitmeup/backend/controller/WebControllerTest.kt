package com.hitmeup.backend.controller

import com.hitmeup.backend.service.FirestoreHitsService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class WebControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var webController: WebController
    private lateinit var hitsService: FirestoreHitsService

    @BeforeEach
    fun setUp() {
        hitsService = mockk(relaxed = true)
        webController = WebController(hitsService)
        ReflectionTestUtils.setField(webController, "domain", "http://localhost:8080")
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build()

        every { hitsService.ensureUrlExists(any()) } returns Unit
    }

    @Test
    fun `메인 페이지 - GET 요청 시 정상 응답`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("badgeForm"))
    }

    @Test
    fun `배지 생성 - POST 요청 시 정상 응답 및 모델 설정`() {
        val url = "https://github.com/username"
        val title = "hits"
        val titleBg = "#555555"
        val countBg = "#79C83D"
        val edgeFlat = "false"

        mockMvc.perform(
            post("/view")
                .param("url", url)
                .param("title", title)
                .param("titleBg", titleBg)
                .param("countBg", countBg)
                .param("edgeFlat", edgeFlat)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("badgeForm"))
            .andExpect(model().attributeExists("markdownCode"))
            .andExpect(model().attributeExists("htmlCode"))
            .andExpect(model().attributeExists("badgeUrl"))
            .andExpect(model().attribute("showResult", true))
    }

    @Test
    fun `API 배지 생성 - JSON 응답 확인`() {
        val url = "https://github.com/username"
        val title = "hits"

        mockMvc.perform(
        post("/api/badge")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"url":"$url","title":"$title","titleBg":"#555555","countBg":"#79C83D","edgeFlat":false}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.badgeUrl").exists())
            .andExpect(jsonPath("$.data.markdownCode").exists())
            .andExpect(jsonPath("$.data.htmlCode").exists())
    }
}