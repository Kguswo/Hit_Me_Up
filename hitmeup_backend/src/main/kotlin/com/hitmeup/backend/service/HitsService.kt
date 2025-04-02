package com.hitmeup.backend.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class HitsService(private val redisTemplate: RedisTemplate<String, Long>) {

    // URL에 대한 방문자 수 증가
    fun incrementHits(url: String): Long {
        val key = "hits:$url"
        return redisTemplate.opsForValue().increment(key) ?: 0
    }

    // URL에 대한 현재 방문자 수 조회
    fun getHits(url: String): Long {
        val key = "hits:$url"
        return redisTemplate.opsForValue().get(key) ?: 0
    }
}