package com.example.staff.permission

interface Account {
    /**
     *
     * ID пользователя
     */
    val id: Int

    /**
     *
     * Список групп пользователя
     */
    val groups: List<Int>
}