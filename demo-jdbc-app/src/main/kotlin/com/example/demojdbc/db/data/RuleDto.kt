package com.example.demojdbc.db.data

import java.util.*

data class RuleDto(
    val id: String = UUID.randomUUID().toString(),
    val channel: String,
    val surface: String,
)
