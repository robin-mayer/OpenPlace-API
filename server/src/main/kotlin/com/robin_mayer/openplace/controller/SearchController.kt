package com.robin_mayer.openplace.controller

import com.robin_mayer.openplace.model.dto.AddressDTO
import com.robin_mayer.openplace.service.SearchService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SearchController(
    private val searchService: SearchService
) {

    @Value("\${api.key}")
    private val apiKey: String? = null

    @GetMapping("/search")
    fun search(
        @RequestHeader("API-Key") key: String,
        @RequestParam(value = "q") query: String,
        @RequestParam(value = "limit", required = false) limit: Int?
    ): ResponseEntity<List<AddressDTO>> {
        if(key != apiKey) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val results = searchService.search(query, limit)
        return ResponseEntity.ok(results)
    }
}