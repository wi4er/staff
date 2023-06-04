package com.example.staff.resolver

data class UserResolver(
    val id: Int,
    val login: String,
    val group: MutableList<Int> = mutableListOf(),
    val contact: MutableList<UserContactResolver> = mutableListOf(),
    val provider: MutableMap<String, String> = mutableMapOf(),
    val property: MutableList<UserPropertyResolver> = mutableListOf(),
)
