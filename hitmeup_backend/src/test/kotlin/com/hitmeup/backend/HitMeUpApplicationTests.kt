package com.hitmeup.backend

import com.hitmeup.backend.service.FirestoreHitsService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HitMeUpApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var hitsService: FirestoreHitsService

    @BeforeEach
    fun setUp() {
        `when`(hitsService.incrementHits(any())).thenReturn(1L)
        `when`(hitsService.getHits(any())).thenReturn(1L)
        `when`(hitsService.ensureUrlExists(any())).then { }
    }

    @Test
    fun contextLoads() {
    }

    @Test
    fun `컨텍스트 로드`() {
    }

    @Test
    fun `메인 페이지 로드`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `배지 생성 및 카운트 증가 통합 테스트`() {
        val testUrl = "https://github.com/test-integration"
        val encodedUrl = java.net.URLEncoder.encode(testUrl, "UTF-8")

        mockMvc.perform(get("/api/count/increment?url=$encodedUrl"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/count?url=$encodedUrl"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.count").value(1))
    }

    @Test
    fun `웹 인터페이스를 통한 배지 생성 통합 테스트`() {
        val url = "https://github.com/web-interface-test"

        mockMvc.perform(
            post("/view")
                .param("url", url)
                .param("title", "visitors")
                .param("titleBg", "#000000")
                .param("countBg", "#FF0000")
                .param("edgeFlat", "true")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("markdownCode"))
            .andExpect(model().attributeExists("htmlCode"))
            .andExpect(model().attributeExists("badgeUrl"))
            .andExpect(model().attribute("showResult", true))
    }
}