package com.robin_mayer.openplace.service

import com.robin_mayer.openplace.model.AddressFilter
import com.robin_mayer.openplace.model.dto.AddressDTO
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    val rowMapper = RowMapper { rs, _ ->
        AddressDTO(
            rs.getString("street"),
            rs.getString("house_number"),
            rs.getString("post_code"),
            rs.getString("city")
        )
    }

    fun search(query: String, limit: Int?): List<AddressDTO> {
        if(limit != null && limit > 100) {
            throw IllegalArgumentException("Limit cannot be greater than 100")
        }

        val filter = convertToAddressFilter(query)
        val sql = StringBuilder("SELECT * FROM addresses WHERE 1=1")
        val params = mutableMapOf<String, Any>()

        filter.street?.let {
            sql.append(" AND LOWER(street) LIKE LOWER(:street)")
            params["street"] = it
        }
        filter.houseNumber?.let {
            sql.append(" AND LOWER(house_number) LIKE LOWER(:houseNumber)")
            params["houseNumber"] = it
        }
        filter.postCode?.let {
            sql.append(" AND LOWER(post_code) LIKE LOWER(:postCode)")
            params["postCode"] = it
        }
        filter.city?.let {
            sql.append(" AND LOWER(city) LIKE LOWER(:city)")
            params["city"] = it
        }

        sql.append(" ORDER BY id")
        sql.append(" LIMIT :limit")
        params["limit"] = limit ?: 5

        val result = namedParameterJdbcTemplate.query(
            sql.toString(),
            params,
            rowMapper
        )

        return result
    }

    fun convertToAddressFilter(query: String): AddressFilter {
        val tokens = query
            .replace(",", "")
            .replace(".", "")
            .lowercase()
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Query is empty")
        }

        val addressFilter = AddressFilter()

        tokens.forEach { token ->
            if (addressFilter.houseNumber == null) {
                if (token.any { it.isDigit() }) {
                    if (token.length > 3) {
                        addressFilter.postCode = token
                    } else {
                        addressFilter.houseNumber = token
                    }
                } else {
                    if (addressFilter.postCode != null) {
                        addressFilter.city = token
                    } else {
                        if (addressFilter.street != null) {
                            addressFilter.street = addressFilter.street?.dropLast(1) + " " + token
                        } else {
                            addressFilter.street = token
                        }
                    }
                }
            } else {
                if (token.any { it.isDigit() }) {
                    addressFilter.postCode = token
                } else {
                    addressFilter.city = token
                }
            }
        }
        return addressFilter
    }
}