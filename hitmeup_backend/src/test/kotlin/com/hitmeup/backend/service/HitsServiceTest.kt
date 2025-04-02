package com.hitmeup.backend.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

class HitsServiceTest {

    private lateinit var redisTemplate: RedisTemplate<String, Long>
    private lateinit var valueOperations: ValueOperations<String, Long>
    private lateinit var hitsService: HitsService

    @BeforeEach
    fun setUp() {
        valueOperations = mockk()
        redisTemplate = mockk()
        every { redisTemplate.opsForValue() } returns valueOperations

        hitsService = HitsService(redisTemplate)
    }

    @Test
    fun `incrementHits - 새 URL인 경우 카운트를 1로 설정`() {
        // given
        val url = "https://github.com/username"
        every { valueOperations.increment("hits:$url") } returns 1L

        // when
        val result = hitsService.incrementHits(url)

        // then
        assertEquals(1L, result)
        verify { valueOperations.increment("hits:$url") }
    }

    @Test
    fun `incrementHits - 기존 URL인 경우 카운트 증가`() {
        // given
        val url = "https://github.com/username"
        every { valueOperations.increment("hits:$url") } returns 42L

        // when
        val result = hitsService.incrementHits(url)

        // then
        assertEquals(42L, result)
        verify { valueOperations.increment("hits:$url") }
    }

    @Test
    fun `getHits - URL이 존재하는 경우 카운트 반환`() {
        // given
        val url = "https://github.com/username"
        every { valueOperations.get("hits:$url") } returns 42L

        // when
        val result = hitsService.getHits(url)

        // then
        assertEquals(42L, result)
        verify { valueOperations.get("hits:$url") }
    }

    @Test
    fun `getHits - URL이 존재하지 않는 경우 0 반환`() {
        // given
        val url = "https://github.com/username"
        every { valueOperations.get("hits:$url") } returns null

        // when
        val result = hitsService.getHits(url)

        // then
        assertEquals(0L, result)
        verify { valueOperations.get("hits:$url") }
    }
}