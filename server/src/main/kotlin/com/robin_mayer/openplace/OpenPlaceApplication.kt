package com.robin_mayer.openplace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class OpenPlaceApplication

fun main(args: Array<String>) {
	runApplication<OpenPlaceApplication>(*args)
}
