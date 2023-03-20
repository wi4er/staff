package com.example.staff

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.sql.DataSource

@SpringBootApplication
class StaffApplication(
    dataSource: DataSource,
) {
    init {
        Database.connect(dataSource)

		val flyway = Flyway.configure().dataSource(dataSource).load()
		flyway.migrate()
    }
}

fun main(args: Array<String>) {
    runApplication<StaffApplication>(*args)
}
