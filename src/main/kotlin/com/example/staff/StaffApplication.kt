package com.example.staff

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DBConfig {
    @Bean
    fun getConnection(): DataSource {
        val hikariConfig = HikariConfig("src/main/resources/db.properties")

        return HikariDataSource(hikariConfig)
    }
}

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
