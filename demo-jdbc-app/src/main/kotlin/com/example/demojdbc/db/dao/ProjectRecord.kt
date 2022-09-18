package com.example.demojdbc.db.dao

import org.jooq.JSONB
import java.time.OffsetDateTime
import java.util.*

data class ProjectRecord(
    val id: UUID,
    val projectId: String,
    val rules: JSONB,
    val version: Long,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val createdBy: String,
    val updatedBy: String,
)
