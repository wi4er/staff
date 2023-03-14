package com.example.staff

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StaffApplication {

	constructor() {
		Database.connect(
			"jdbc:postgresql://localhost:5432/postgres",
			driver = "org.postgresql.Driver",
			user = "postgres",
			password = "example"
		)
	}
}

fun main(args: Array<String>) {
	runApplication<StaffApplication>(*args)
}
