package com.example.demojdbc

import com.example.demojdbc.common.BaseDatabaseTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JdbcTemplateTest : BaseDatabaseTest() {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "select current_schema()",
            "show search_path",
            "select version()",
            "select uuid_generate_v4()",
        ]
    )
    fun info(sql: String) {
        val result = template.queryForList(sql)
        log.info("\nresult = {}", result)
    }
}
