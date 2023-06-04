package com.example.staff.filler.user

import com.example.staff.resolver.UserResolver

interface UserFiller {
    fun fill(map: MutableMap<Int, UserResolver>)
}