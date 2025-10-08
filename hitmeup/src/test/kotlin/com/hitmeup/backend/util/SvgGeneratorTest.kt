package com.hitmeup.backend.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SvgGeneratorTest {

    @Test
    fun `SVG 생성 - 기본 설정으로 테스트`() {

        val title = "hits"
        val titleBg = "#555555"
        val countBg = "79C83D"
        val edgeFlat = false
        val count = 42L

        // when
        val svg = SvgGenerator.generateSvg(title, titleBg, countBg, edgeFlat, count)

        // then
        assertNotNull(svg)
        assertTrue(svg.contains("<svg"))
        assertTrue(svg.contains("</svg>"))
        assertTrue(svg.contains("</svg>"))
        assertTrue(svg.contains(">$count<"))
        assertTrue(svg.contains(">$title<"))
        assertFalse(svg.contains("rx=\"0\"")) // edgeFlat값이 false이므로 rx="0" 있으면 안됨
    }

    @Test
    fun `SVG 생성 - 평평한 모서리로 테스트`() {
        // given
        val title = "visitors"
        val titleBg = "#000000"
        val countBg = "#FF0000"
        val edgeFlat = true
        val count = 100L

        // when
        val svg = SvgGenerator.generateSvg(title, titleBg, countBg, edgeFlat, count)

        // then
        assertNotNull(svg)
        assertTrue(svg.contains("rx=\"0\"")) // edgeFlat이 true이므로 rx="0"이 포함되어야 함
        assertTrue(svg.contains(">$count<"))
        assertTrue(svg.contains(">$title<"))
        assertTrue(svg.contains("fill=\"$titleBg\""))
        assertTrue(svg.contains("fill=\"$countBg\""))
    }
}