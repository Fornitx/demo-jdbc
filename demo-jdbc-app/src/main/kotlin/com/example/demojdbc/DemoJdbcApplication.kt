package com.example.demojdbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoJdbcApplication

fun main(args: Array<String>) {
	runApplication<DemoJdbcApplication>(*args)
}
