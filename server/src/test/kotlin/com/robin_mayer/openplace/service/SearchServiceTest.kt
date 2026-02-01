package com.robin_mayer.openplace.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchServiceTest @Autowired constructor(
    private val searchService: SearchService
){

    @Test
    fun testConvertToAddressFilter_1(){
        // given
        val query = "Hauptstr. 11"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("hauptstr%", addressFilter.street)
        assertEquals("11%", addressFilter.houseNumber)
        assertEquals(null, addressFilter.postCode)
        assertEquals(null,  addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_2(){
        // given
        val query = "HAUPtstraße 85, 80999 München"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("hauptstraße%", addressFilter.street)
        assertEquals("85%", addressFilter.houseNumber)
        assertEquals("80999%", addressFilter.postCode)
        assertEquals("münchen%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_3(){
        // given
        val query = "Berliner Allee 15  13088 Berlin"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("berliner allee%", addressFilter.street)
        assertEquals("15%", addressFilter.houseNumber)
        assertEquals("13088%", addressFilter.postCode)
        assertEquals("berlin%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_4(){
        // given
        val query = "Musterweg"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("musterweg%", addressFilter.street)
        assertEquals(null, addressFilter.houseNumber)
        assertEquals(null, addressFilter.postCode)
        assertEquals(null, addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_5(){
        // given
        val query = "  Lindenstraße   7B ,   10115   Berlin  "

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("lindenstraße%", addressFilter.street)
        assertEquals("7b%", addressFilter.houseNumber)
        assertEquals("10115%", addressFilter.postCode)
        assertEquals("berlin%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_6(){
        // given
        val query = "Friedrichstraße 43A"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("friedrichstraße%", addressFilter.street)
        assertEquals("43a%", addressFilter.houseNumber)
        assertEquals(null, addressFilter.postCode)
        assertEquals(null, addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_7(){
        // given
        val query = "12345 Musterstadt"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals(null, addressFilter.street)
        assertEquals(null, addressFilter.houseNumber)
        assertEquals("12345%", addressFilter.postCode)
        assertEquals("musterstadt%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_8(){
        // given
        val query = "Am Bahnhof 5C 54321 Beispielstadt"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("am bahnhof%", addressFilter.street)
        assertEquals("5c%", addressFilter.houseNumber)
        assertEquals("54321%", addressFilter.postCode)
        assertEquals("beispielstadt%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_9(){
        // given
        val query = "  . ,"

        // when / then
        assertThrows<IllegalArgumentException> {
            searchService.convertToAddressFilter(query)
        }
    }

    @Test
    fun testConvertToAddressFilter_10(){
        // given
        val query = "13 70190 Stuttgart"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals(null, addressFilter.street)
        assertEquals("13%", addressFilter.houseNumber)
        assertEquals("70190%", addressFilter.postCode)
        assertEquals("stuttgart%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_11(){
        // given
        val query = "Hauptstr. 13 70190 Stuttgart"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("hauptstr%", addressFilter.street)
        assertEquals("13%", addressFilter.houseNumber)
        assertEquals("70190%", addressFilter.postCode)
        assertEquals("stuttgart%", addressFilter.city)
    }

    @Test
    fun testConvertToAddressFilter_12(){
        // given
        val query = "Plettenbergstr. 1 Stuttgart"

        // when
        val addressFilter = searchService.convertToAddressFilter(query)

        // then
        assertEquals("plettenbergstr%", addressFilter.street)
        assertEquals("1%", addressFilter.houseNumber)
        assertEquals(null, addressFilter.postCode)
        assertEquals("stuttgart%", addressFilter.city)
    }
}