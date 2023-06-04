package com.example.staff.filler.user

import com.example.staff.model.User2PointEntity
import com.example.staff.resolver.UserPointResolver
import com.example.staff.resolver.UserResolver
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserPointFiller: UserFiller {
    override fun fill(map: MutableMap<Int, UserResolver>) {
        User2PointEntity
            .select { User2PointEntity.user inList map.keys }
            .forEach { row ->
                map[row[User2PointEntity.user].value]?.property?.add(
                    UserPointResolver(
                        property = row[User2PointEntity.property].value,
                        value = row[User2PointEntity.point].value,
                    )
                )
            }
    }
}