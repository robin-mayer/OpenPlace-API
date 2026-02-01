package com.robin_mayer.openplace.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ApiKeyStartupCheck: ApplicationRunner {

    @Value("\${api.key}")
    private val apiKey: String? = null

    override fun run(args: ApplicationArguments) {
        if(apiKey.isNullOrBlank() || apiKey == "<empty>") {
            throw IllegalStateException("api.key is not set properly. Please set a valid API key.")
        }
    }
}