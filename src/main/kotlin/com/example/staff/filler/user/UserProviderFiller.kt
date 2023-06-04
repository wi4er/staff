package com.example.staff.filler.user

import com.example.staff.model.User2ProviderEntity
import com.example.staff.resolver.UserResolver
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserProviderFiller: UserFiller {
    override fun fill(map: MutableMap<Int, UserResolver>) {
        User2ProviderEntity
            .select { User2ProviderEntity.user inList map.keys }
            .forEach { row ->
                map[row[User2ProviderEntity.user].value]?.let {
                    it.provider[row[User2ProviderEntity.provider].value] = row[User2ProviderEntity.hash]
                }
            }
    }
}