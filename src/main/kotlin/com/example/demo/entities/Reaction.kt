package com.example.demo.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Table

@Embeddable
data class Reaction (
    var name: String = "",
    var reaction: String = ""
)