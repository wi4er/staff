package com.example.staff.permission

import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component

@Component
class AccountFactory(
    val tokenService: TokenService,
) {
    /**
     *
     * Разбор содержимого токена в объект аккаунта
     *
     * @param decode - разобранный токен
     */
    private fun decodeToken(decode: DecodedJWT): Account {
        return UserAccount(
            id = decode.getClaim("id").asInt(),
            groups = decode.getClaim("groups").asList<Int>(Int::class.java)
        )
    }

    /**
     *
     * Создание объекта аккаунта пользователя из строкового токена
     *
     * @param token - токен авторизации
     * @throws JWTDecodeException - исключение невалидного токена
     */
    fun createFromToken(token: String): Account = tokenService.parse(token, ::decodeToken)

    /**
     *
     */
    fun createToken(account: Account): String {
        return tokenService.encrypt { token ->
            token.withClaim("id", account.id)
            token.withArrayClaim("groups", account.groups.toTypedArray())
        }
    }
}