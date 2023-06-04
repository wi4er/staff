package com.example.staff.filler.user

import com.example.staff.model.User2GroupEntity
import com.example.staff.resolver.UserResolver
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserGroupFiller: UserFiller {
    override fun fill(map: MutableMap<Int, UserResolver>) {
        User2GroupEntity
            .select { User2GroupEntity.user inList map.keys }
            .forEach { group ->
                map[group[User2GroupEntity.user].value]
                    ?.group
                    ?.add(group[User2GroupEntity.group].value)
            }
    }
}