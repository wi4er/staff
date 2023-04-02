package com.example.staff.input


class PermissionInput {
    val group: Int? = null
}

class UserInput {
    var id: Int? = null
    var login: String = ""
    var group: List<Int> = listOf()
    var permission: List<PermissionInput> = listOf()
}