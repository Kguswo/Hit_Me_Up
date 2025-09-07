package com.hitmeup.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // 테스트용 프로파일을 사용합니다
class HitMeUpApplicationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
    }

    @Test
    fun `컨텍스트 로드`() {
        // 스프링 컨텍스트가 정상적으로 로드되는지 확인합니다
    }

    @Test
    fun `메인 페이지 로드`() {
        // when & then
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(view().name("index"))
    }

    @Test
    fun `배지 생성 및 카운트 증가 통합 테스트`() {
        // given
        val url = "https://github.com/test-integration"
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")

        // 초기 카운트 확인
        mockMvc.perform(get("/api/count?url=$encodedUrl"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.count").value(0))

        // 배지 요청으로 카운트 증가
        mockMvc.perform(get("/api/count/increment?url=$encodedUrl"))
            .andExpect(status().isOk)
            // 콘텐츠 타입 검사를 수정 (charset 포함)
            .andExpect(content().contentTypeCompatibleWith("image/svg+xml"))

        // 증가된 카운트 확인
        mockMvc.perform(get("/api/count?url=$encodedUrl"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.count").value(1))
    }

    @Test
    fun `웹 인터페이스를 통한 배지 생성 통합 테스트`() {
        // given
        val url = "https://github.com/web-interface-test"

        // when & then
        mockMvc.perform(
            post("/generate")
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