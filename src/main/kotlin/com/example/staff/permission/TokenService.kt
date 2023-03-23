package com.example.staff.permission

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component

/**
 *
 */
@Component
class TokenService {
    /**
     *
     * алгоритм кодирования токена
     */
    private val algorithm = Algorithm.HMAC256("secret")

    /**
     *
     * Разбор токена и превращение его содержимого в объект R
     *
     * @param token - токен авторизации
     * @param parse - функция положительной валидации токена
     */
    fun <R>parse(
        token: String,
        parse: (DecodedJWT) -> R
    ): R = JWT
        .require(algorithm)
        .build()
        .verify(token)
        .let { parse(it) }

    /**
     *
     * Генерация токена и добавление в него полей
     *
     * @param encode - функция формирования содержимого
     */
    fun encrypt(
        encode: (JWTCreator.Builder) -> JWTCreator.Builder
    ): String = JWT
        .create()
        .let { encode(it) }
        .sign(algorithm)
}