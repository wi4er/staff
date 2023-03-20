package com.example.staff

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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