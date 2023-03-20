package com.example.staff.resolver

data class UserResolver(
    val id: Int,
    val login: String,
    val group: MutableList<Int>,
)
