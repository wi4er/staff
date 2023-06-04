package com.example.staff.filler.user

import com.example.staff.model.User2StringEntity
import com.example.staff.resolver.UserResolver
import com.example.staff.resolver.UserStringResolver
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserStringFiller: UserFiller {
    private fun MutableMap<Int, UserResolver>.add(row: ResultRow) {
        get(row[User2StringEntity.user].value)
            ?.property
            ?.add(
                UserStringResolver(
                    property = row[User2StringEntity.property].value,
                    value = row[User2StringEntity.value],
                    lang = row[User2StringEntity.lang]?.value,
                )
            )
    }

    override fun fill(map: MutableMap<Int, UserResolver>) {
        User2StringEntity
            .select { User2StringEntity.user inList map.keys }
            .forEach { map.add(it) }
    }
}