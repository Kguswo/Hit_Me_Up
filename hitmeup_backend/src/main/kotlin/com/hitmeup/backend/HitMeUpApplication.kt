package com.hitmeup.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HitMeUpApplication

fun main(args: Array<String>) {
	runApplication<HitMeUpApplication>(*args)
}
