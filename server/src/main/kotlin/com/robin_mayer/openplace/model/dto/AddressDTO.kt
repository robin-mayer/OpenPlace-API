package com.robin_mayer.openplace.model.dto

data class AddressDTO (
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val city: String
)