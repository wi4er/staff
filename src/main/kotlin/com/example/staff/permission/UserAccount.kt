package com.example.staff.permission

data class UserAccount(
    /**
     *
     * ID пользователя
     */
    override val id: Int,

    /**
     *
     * Список групп пользователя
     */
    override val groups: List<Int>,
): Account
