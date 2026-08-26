package com.robin_mayer.openplace.service

import com.robin_mayer.openplace.repository.AddressRepository
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.EventListener
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.NumberFormat
import java.util.Locale

@Service
class ImportService(
    private val jdbcTemplate: JdbcTemplate,
    private val addressRepository: AddressRepository,
    @Value($$"${import.auto.url:}") private val autoImportUrl: String,
) {

    @Lazy
    @Autowired
    private lateinit var self: ImportService

    private val log = LoggerFactory.getLogger(javaClass)

    private var importRunning: Boolean = false

    private val IDX_STREET_ID = "idx_street_id"
    private val IDX_HOUSE_NUMBER_ID = "idx_house_number_id"
    private val IDX_POST_CODE_ID = "idx_post_code_id"
    private val IDX_CITY_ID = "idx_city_id"

    @EventListener(ApplicationReadyEvent::class)
    fun autoImport() {
        if(autoImportUrl.isNotBlank() && addressRepository.count() == 0L) {
            startImport(autoImportUrl)
        }
    }

    fun startImport(downloadUrl: String): Boolean {
        if (importRunning) {
            return false
        } else {
            importRunning = true
            self.import(downloadUrl)
            return true
        }
    }

    @Async
    fun import(downloadUrl: String) {
        try {
            cleanup()
            log.info("Importing $downloadUrl")
            if (!downloadFile(downloadUrl)) {
                log.error("Download URL could not be found")
                throw Exception("File download failed")
            }
            convertFile()
            deleteIndexes()
            self.clearDatabase()
            importFile()
            createIndexes()
            log.info("Import of $downloadUrl completed")
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            cleanup()
            importRunning = false
        }
    }

    fun downloadFile(downloadUrl: String): Boolean {
        log.info("Downloading $downloadUrl")
        if (!downloadUrl.endsWith(".osm.pbf")) {
            return false
        }

        RestTemplate().execute(
            URI(downloadUrl),
            HttpMethod.GET,
            null
        ) { response ->
            response.body.use { input ->
                Files.copy(
                    input,
                    Paths.get("data.osm.pbf"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            null
        }

        log.info("Finished downloading $downloadUrl")
        return true
    }

    fun convertFile() {
        log.info("Filtering file to only include addresses")
        val filterProcessBuilder = ProcessBuilder(
            "osmium", "tags-filter",
            "data.osm.pbf",

            // Nodes
            "n/addr:housenumber",
            "n/addr:street",
            "n/addr:postcode",
            "n/addr:city",

            // Ways
            "w/addr:housenumber",
            "w/addr:street",
            "w/addr:postcode",
            "w/addr:city",

            // Relations
            "r/addr:housenumber",
            "r/addr:street",
            "r/addr:postcode",
            "r/addr:city",

            "-o", "filtered.osm.pbf"
        )
        filterProcessBuilder.start().waitFor()
        log.info("Finished filtering file")

        log.info("Converting file to GeoJSON")
        val conversionProcessBuilder = ProcessBuilder(
            "osmium",
            "export",
            "filtered.osm.pbf",
            "-o",
            "data.geojson",
        )
        conversionProcessBuilder.start().waitFor()
        log.info("Finished converting file to GeoJSON")
    }

    fun deleteIndexes() {
        log.info("All previous indexes will be dropped before import")
        jdbcTemplate.execute("DROP INDEX IF EXISTS $IDX_STREET_ID")
        jdbcTemplate.execute("DROP INDEX IF EXISTS $IDX_HOUSE_NUMBER_ID")
        jdbcTemplate.execute("DROP INDEX IF EXISTS $IDX_POST_CODE_ID")
        jdbcTemplate.execute("DROP INDEX IF EXISTS $IDX_CITY_ID")
        log.info("All indexes dropped")
    }

    fun clearDatabase() {
        log.info("Clearing existing addresses from database")
        jdbcTemplate.update("TRUNCATE TABLE addresses RESTART IDENTITY")
        log.info("All existing addresses deleted")
    }

    fun importFile() {
        log.info("Importing file into database")

        val file = File("data.geojson")
        file.bufferedReader().useLines { lines ->
            var processed = 0L
            val numberFormat = NumberFormat.getInstance(Locale.US)
            val start = System.currentTimeMillis()
            var lastLogTime = start

            lines.forEach { line ->
                if (processed % 100000 == 0L) {
                    val currentTime = System.currentTimeMillis()
                    val timeDiff = formatMillisToTime(currentTime - lastLogTime)
                    val totalTimeDiff = formatMillisToTime(currentTime - start)
                    lastLogTime = System.currentTimeMillis()
                    log.info(
                        "${numberFormat.format(processed)} lines processed. " +
                                "Time for last 100,000: ${timeDiff}. " +
                                "Total time: ${totalTimeDiff}."
                    )
                }
                processed++

                val json = runCatching { JSONObject(line) }.getOrNull() ?: return@forEach
                val properties = json.optJSONObject("properties") ?: return@forEach
                val geometry = json.optJSONObject("geometry") ?: return@forEach
                val coordinates = geometry.optJSONArray("coordinates") ?: return@forEach
                
                val street = properties.optString("addr:street", null)
                val houseNumber = properties.optString("addr:housenumber", null)
                val postCode = properties.optString("addr:postcode", null)
                val city = properties.optString("addr:city", null)
                var latitude = coordinates.optDouble(1)
                var longitude = coordinates.optDouble(0)
                if(latitude.isNaN() || longitude.isNaN()) {
                    latitude = coordinates.optJSONArray(0).optDouble(1)
                    longitude = coordinates.optJSONArray(0).optDouble(0)
                    if(latitude.isNaN() || longitude.isNaN()) {
                        return@forEach
                    }
                }
                
                if (!street.isNullOrEmpty() && !houseNumber.isNullOrEmpty() && !postCode.isNullOrEmpty() && !city.isNullOrEmpty()) {
                    addressRepository.insertIgnore(street, houseNumber, postCode, city, latitude, longitude)
                }
            }
        }

        log.info("Finished importing file into database")
    }

    fun createIndexes() {
        log.info("Creating indexes on new address table")
        jdbcTemplate.execute("CREATE INDEX $IDX_STREET_ID ON addresses (LOWER(street) text_pattern_ops, id)")
        jdbcTemplate.execute("CREATE INDEX $IDX_HOUSE_NUMBER_ID ON addresses (LOWER(house_number) text_pattern_ops, id)")
        jdbcTemplate.execute("CREATE INDEX $IDX_POST_CODE_ID ON addresses (LOWER(post_code) text_pattern_ops, id)")
        jdbcTemplate.execute("CREATE INDEX $IDX_CITY_ID ON addresses (LOWER(city) text_pattern_ops, id)")
        log.info("Finished creating indexes on address table")
    }

    fun cleanup() {
        Files.deleteIfExists(Paths.get("data.osm.pbf"))
        Files.deleteIfExists(Paths.get("filtered.osm.pbf"))
        Files.deleteIfExists(Paths.get("data.geojson"))
        log.info("Cleaned up temporary files")
    }

    private fun formatMillisToTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        if(hours > 0) {
            return "${hours}h, ${minutes}m, ${seconds}s"
        } else if (minutes > 0) {
            return "${minutes}m, ${seconds}s"
        } else {
            return "${seconds}s"
        }
    }
}