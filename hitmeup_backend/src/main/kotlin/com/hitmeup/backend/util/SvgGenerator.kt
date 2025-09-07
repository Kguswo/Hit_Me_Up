package com.hitmeup.backend.util

object SvgGenerator {

    fun generateSvg(title: String, titleBg: String, countBg: String, edgeFlat: Boolean, count: Long): String {
        val borderRadius = if (edgeFlat) "0" else "3"
        val countText = count.toString()

        val titleWidth = getTextWidth(title)
        val countWidth = countText.length * 7 + 10
        val width = titleWidth + countWidth

        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="20">
              <linearGradient id="smooth" x2="0" y2="100%">
                <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                <stop offset="1" stop-opacity=".1"/>
              </linearGradient>
              <mask id="round">
                <rect width="$width" height="20" rx="$borderRadius" ry="$borderRadius" fill="#fff"/>
              </mask>
              <g mask="url(#round)">
                <rect width="$titleWidth" height="20" fill="$titleBg"/>
                <rect x="$titleWidth" width="$countWidth" height="20" fill="$countBg"/>
                <rect width="$width" height="20" fill="url(#smooth)"/>
              </g>
              <g fill="#fff" text-anchor="middle" font-family="Verdana,DejaVu Sans,Geneva,sans-serif" font-size="11">
                <text x="${titleWidth / 2}" y="15" fill="#010101" fill-opacity=".3">$title</text>
                <text x="${titleWidth / 2}" y="14" fill="#fff">$title</text>
                <text x="${titleWidth + countWidth / 2}" y="15" fill="#010101" fill-opacity=".3">$countText</text>
                <text x="${titleWidth + countWidth / 2}" y="14" fill="#fff">$countText</text>
              </g>
            </svg>
        """.trimIndent()
    }

    private fun getTextWidth(text: String): Int {
        var width = 0
        for (c in text) {
            width += if (c.code > 255) 14 else 7
        }
        return width + 10
    }
}