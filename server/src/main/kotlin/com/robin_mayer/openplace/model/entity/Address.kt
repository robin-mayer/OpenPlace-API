package com.robin_mayer.openplace.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "addresses")
@Suppress("Unused")
class Address(
    @Column(nullable = false)
    val street: String,
    @Column(nullable = false)
    val houseNumber: String,
    @Column(nullable = false)
    val postCode: String,
    @Column(nullable = false)
    val city: String,
    @Column(nullable = false)
    val latitude: Double,
    @Column(nullable = false)
    val longitude: Double
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}