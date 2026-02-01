package com.robin_mayer.openplace.controller

import com.robin_mayer.openplace.model.dto.ImportDTO
import com.robin_mayer.openplace.service.ImportService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ImportController(
    private val importService: ImportService
) {

    @Value("\${api.key}")
    private val apiKey: String? = null

    @Suppress("JvmTaintAnalysis")
    @PostMapping("/import")
    fun import(
        @RequestHeader("API-Key") key: String,
        @RequestBody input: ImportDTO
    ): ResponseEntity<Void> {
        if(key != apiKey) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val importStarted = importService.startImport(input.downloadUrl)
        if(!importStarted) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
        return ResponseEntity.noContent().build()
    }
}