package com.example.staff.resolver

data class UserResolver(
    val id: Int,
    val login: String,
    val group: MutableList<Int>,
    val contact: MutableList<UserContactResolver>,
    val provider: MutableMap<String, String> = mutableMapOf()
)
