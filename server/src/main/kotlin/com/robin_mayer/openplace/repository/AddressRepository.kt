package com.robin_mayer.openplace.repository

import com.robin_mayer.openplace.model.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface AddressRepository: JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

    @Modifying
    @Transactional
    @Query(
        value = """
            INSERT INTO addresses (street, house_number, post_code, city, latitude, longitude)
            VALUES (:street, :houseNumber, :postCode, :city, :latitude, :longitude)
            ON CONFLICT (street, house_number, post_code, city)
            DO NOTHING
        """,
        nativeQuery = true
    )
    fun insertIgnore(
        @Param("street") street: String,
        @Param("houseNumber") houseNumber: String,
        @Param("postCode") postCode: String,
        @Param("city") city: String,
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double
    )
}