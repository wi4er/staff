package com.example.staff.resolver

data class UserPointResolver(
    override val property: String,
    override val value: String,
): UserPropertyResolver
