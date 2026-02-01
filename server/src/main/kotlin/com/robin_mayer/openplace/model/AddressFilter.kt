package com.robin_mayer.openplace.model


class AddressFilter {
    var street: String? = null
        set(value) {
            field = value?.lowercase()?.trim().plus("%")
        }

    var houseNumber: String? = null
        set(value) {
            field = value?.lowercase()?.trim().plus("%")
        }

    var postCode: String? = null
        set(value) {
            field = value?.lowercase()?.trim().plus("%")
        }

    var city: String? = null
        set(value) {
            field = value?.lowercase()?.trim().plus("%")
        }
}