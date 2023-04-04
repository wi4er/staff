package com.example.staff.resolver

data class UserContactResolver(
    val contact: String,
    val value: String,
)

data class UserResolver(
    val id: Int,
    val login: String,
    val group: MutableList<Int>,
    val contact: MutableList<UserContactResolver>,
)
