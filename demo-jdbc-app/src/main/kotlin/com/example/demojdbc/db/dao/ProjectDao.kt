package com.example.demojdbc.db.dao

import com.example.demojdbc.db.data.RuleDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component

@Component
class ProjectDao(
    private val dslContext: DSLContext,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val APP_DIR_USER = "AppDir"
    }

    private val projectTable = DSL.table("project")

    private val projectIdField = DSL.field("id", SQLDataType.UUID)
    private val projectProjectIdField = DSL.field("project_id", SQLDataType.VARCHAR)
    private val projectRulesField = DSL.field("rules", SQLDataType.JSONB)
    private val projectVersionField = DSL.field("version", SQLDataType.BIGINT)
    private val projectCreatedAtField = DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val projectUpdatedAtField = DSL.field("updated_at", SQLDataType.TIMESTAMPWITHTIMEZONE)
    private val projectCreatedByField = DSL.field("created_by", SQLDataType.VARCHAR)
    private val projectUpdatedByField = DSL.field("updated_by", SQLDataType.VARCHAR)

    private val projectAllFields = listOf(
        projectIdField,
        projectProjectIdField,
        projectRulesField,
        projectVersionField,
        projectCreatedAtField,
        projectUpdatedAtField,
        projectCreatedByField,
        projectUpdatedByField
    )

    private val fixedProjectVersionField = DSL.field("project.version", SQLDataType.BIGINT)
    private val fixedProjectUpdatedByField = DSL.field("project.updated_by", SQLDataType.VARCHAR)

    fun findByProjectId(projectId: String): ProjectRecord? {
        return dslContext.selectFrom(projectTable)
            .where(projectProjectIdField.eq(projectId))
            .fetchOneInto(ProjectRecord::class.java)
    }

    fun upsert(projectId: String, rules: List<RuleDto>, user: String, version: Long? = null): ProjectRecord {
        if (version == null) {
            return dslContext.insertInto(
                projectTable,
                projectProjectIdField,
                projectRulesField,
                projectCreatedByField,
                projectUpdatedByField,
            )
                .values(projectId, JSONB.jsonb(objectMapper.writeValueAsString(rules)), user, user)
                .returning(projectAllFields)
                .fetchOneInto(ProjectRecord::class.java)!!
        } else {
            return dslContext.insertInto(
                projectTable,
                projectProjectIdField,
                projectRulesField,
                projectCreatedByField,
                projectUpdatedByField,
            )
                .values(projectId, JSONB.jsonb(objectMapper.writeValueAsString(rules)), user, user)
                .onConflict(projectProjectIdField)
                .doUpdate()
                .set(projectRulesField, DSL.excluded(projectRulesField))
                .set(projectVersionField, fixedProjectVersionField.plus(1))
                .set(projectUpdatedAtField, DSL.currentOffsetDateTime())
                .set(projectUpdatedByField, user)
                .where(fixedProjectVersionField.eq(version))
                .returning(projectAllFields)
                .fetchOneInto(ProjectRecord::class.java)
                ?: throw OptimisticLockingFailureException("Version $version doesn't exists")
        }
    }

    fun upsertFromAppDir(projectId: String, rules: List<RuleDto>): ProjectRecord {
        return dslContext.insertInto(
            projectTable,
            projectProjectIdField,
            projectRulesField,
            projectCreatedByField,
            projectUpdatedByField,
        )
            .values(projectId, JSONB.jsonb(objectMapper.writeValueAsString(rules)), APP_DIR_USER, APP_DIR_USER)
            .onConflict(projectProjectIdField)
            .doUpdate()
            .set(projectRulesField, DSL.excluded(projectRulesField))
            .set(projectVersionField, fixedProjectVersionField.plus(1))
            .set(projectUpdatedAtField, DSL.currentOffsetDateTime())
            .where(fixedProjectUpdatedByField.eq(APP_DIR_USER))
            .returning(projectAllFields)
            .fetchOneInto(ProjectRecord::class.java)
            ?: throw OptimisticLockingFailureException("Project user is not '$APP_DIR_USER'")
    }
}
