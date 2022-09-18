package com.example.demojdbc

import com.example.demojdbc.common.BaseDatabaseTest
import com.example.demojdbc.db.dao.ProjectDao
import com.example.demojdbc.db.dao.ProjectDao.Companion.APP_DIR_USER
import org.assertj.core.api.Assertions.assertThat
import org.jooq.JSONB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException

@SpringBootTest
class ProjectDaoTest : BaseDatabaseTest() {
    companion object {
        const val USER_1 = "USER_1"
        const val USER_2 = "USER_2"
    }

    @Autowired
    private lateinit var dao: ProjectDao

    @Test
    fun testUpsertNew() {
        val projectId = "proj:1"

        val saved = dao.upsert(projectId, listOf(), USER_1)
        log.info { "saved = $saved" }

        assertEquals(projectId, saved.projectId)
        assertEquals(JSONB.jsonb("[]"), saved.rules)
        assertEquals(1, saved.version)
        assertEquals(USER_1, saved.createdBy)
        assertEquals(USER_1, saved.updatedBy)
        assertEquals(saved.createdAt, saved.updatedAt)
    }

    @Test
    fun testFindByProjectId() {
        val projectId = "proj:2"

        val saved = dao.upsert(projectId, listOf(), USER_1)
        log.info { "saved = $saved" }

        val found = dao.findByProjectId(projectId)
        log.info { "found = $found" }

        assertEquals(saved, found)
    }

    @Test
    fun testUpsertNewDuplicateKey() {
        val projectId = "proj:3"

        val saved = dao.upsert(projectId, listOf(), USER_1)
        log.info { "saved = $saved" }

        assertThrows<DuplicateKeyException> {
            dao.upsert(projectId, listOf(), USER_1)
        }
    }

    @Test
    fun testUpsertNewWithVersion() {
        val projectId = "proj:4"

        val saved = dao.upsert(projectId, listOf(), USER_1, 5)
        log.info { "saved = $saved" }
        assertEquals(1, saved.version)
    }

    @Test
    fun testUpsertExisting() {
        val projectId = "proj:5"

        val saved = dao.upsert(projectId, listOf(), USER_1)
        log.info { "saved = $saved" }

        val updated = dao.upsert(projectId, listOf(), USER_2, saved.version)
        log.info { "updated = $updated" }

        assertEquals(saved.version + 1, updated.version)
        assertEquals(USER_1, updated.createdBy)
        assertEquals(USER_2, updated.updatedBy)
        assertThat(updated.createdAt).isBefore(updated.updatedAt)
    }

    @Test
    fun testUpsertExistingOptimisticLock() {
        val projectId = "proj:6"

        val saved = dao.upsert(projectId, listOf(), USER_1)
        log.info { "saved = $saved" }

        assertThrows<OptimisticLockingFailureException> {
            dao.upsert(projectId, listOf(), USER_2, saved.version + 5)
        }
    }

    @Test
    fun testUpsertFromAppDir() {
        val projectId = "proj:7"

        val saved = dao.upsertFromAppDir(projectId, listOf())
        log.info { "saved = $saved" }

        assertEquals(projectId, saved.projectId)
        assertEquals(1, saved.version)
        assertEquals(APP_DIR_USER, saved.createdBy)
        assertEquals(APP_DIR_USER, saved.updatedBy)
        assertEquals(saved.createdAt, saved.updatedAt)
    }

    @Test
    fun testUpsertFromAppDirExisting() {
        val projectId = "proj:8"

        val saved = dao.upsertFromAppDir(projectId, listOf())
        log.info { "saved = $saved" }

        val updated = dao.upsertFromAppDir(projectId, listOf())
        log.info { "updated = $updated" }

        assertEquals(saved.version + 1, updated.version)
        assertEquals(APP_DIR_USER, updated.createdBy)
        assertEquals(APP_DIR_USER, updated.updatedBy)
        assertThat(updated.createdAt).isBefore(updated.updatedAt)

        assertThrows<OptimisticLockingFailureException> {
            val updated2 = dao.upsert(projectId, listOf(), USER_1, saved.version + 5)
            log.info { "updated2 = $updated2" }
        }
    }

    @Test
    fun testUpsertFromAppDirOptimisticLock() {
        val projectId = "proj:9"

        val saved = dao.upsertFromAppDir(projectId, listOf())
        log.info { "saved = $saved" }

        val updated = dao.upsert(projectId, listOf(), USER_1, saved.version)
        log.info { "updated = $updated" }

        assertThrows<OptimisticLockingFailureException> {
            dao.upsertFromAppDir(projectId, listOf())
        }
    }
}
