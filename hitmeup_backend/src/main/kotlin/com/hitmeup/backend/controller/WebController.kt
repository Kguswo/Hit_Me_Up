package com.hitmeup.backend.controller


import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class WebController {

    @Value("\${app.domain}")
    private lateinit var domain: String

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("badgeForm", BadgeForm())
        model.addAttribute("showResult", false)
        return "index"
    }

    @PostMapping("/generate")
    fun generate(@ModelAttribute badgeForm: BadgeForm, model: Model): String {
        val encodedUrl = java.net.URLEncoder.encode(badgeForm.url, java.nio.charset.StandardCharsets.UTF_8)

        val markdownCode = """
            [![Hits](${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat})](${domain})
        """.trimIndent()

        val htmlCode = """
            <a href="${domain}">
              <img src="${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix(
                "#"
            )
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}" alt="${badgeForm.title}" />
            </a>
        """.trimIndent()

        val badgeUrl = "${domain}/api/count/increment?url=${encodedUrl}&title=${badgeForm.title}&title_bg=${
            badgeForm.titleBg.removePrefix("#")
        }&count_bg=${badgeForm.countBg.removePrefix("#")}&edge_flat=${badgeForm.edgeFlat}"

        model.addAttribute("badgeForm", badgeForm)
        model.addAttribute("markdownCode", markdownCode)
        model.addAttribute("htmlCode", htmlCode)
        model.addAttribute("badgeUrl", badgeUrl)
        model.addAttribute("showResult", true)

        return "index"
    }

    data class BadgeForm(
        var url: String = "",
        var title: String = "hits",
        var titleBg: String = "#555555",
        var countBg: String = "#79C83D",
        var edgeFlat: Boolean = false
    )
}