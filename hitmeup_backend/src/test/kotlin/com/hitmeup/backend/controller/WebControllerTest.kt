package com.hitmeup.backend.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class WebControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var webController: WebController

    @BeforeEach
    fun setUp() {
        webController = WebController()
        // ReflectionTestUtils를 사용하여 private 필드 주입
        ReflectionTestUtils.setField(webController, "domain", "http://localhost:8080")
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build()
    }

    @Test
    fun `메인 페이지 - GET 요청 시 정상 응답`() {
        // when & then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("badgeForm"))
            .andExpect(model().attribute("showResult", false))
    }

    @Test
    fun `배지 생성 - POST 요청 시 정상 응답 및 모델 설정`() {
        // given
        val url = "https://github.com/username"
        val title = "hits"
        val titleBg = "#555555"
        val countBg = "#79C83D"
        val edgeFlat = "false"

        // when & then
        mockMvc.perform(
            post("/generate")
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
}