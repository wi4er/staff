package com.example.staff.controller

import com.example.staff.model.UserEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController {

    @GetMapping("/")
    @CrossOrigin
    fun get(): String {
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(UserEntity)

            val list = UserEntity.selectAll().firstOrNull()

            println(list)
        }

        return "Hello world!!!"
    }

}