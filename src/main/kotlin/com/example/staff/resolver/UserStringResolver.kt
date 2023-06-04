package com.example.staff.resolver

data class UserStringResolver(
    override val property: String,
    override val value: String,
    val lang: String? = null,
): UserPropertyResolver
