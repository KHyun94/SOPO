package com.delivery.sopo.database.room.dto

data class OAuthDto(
    val email: String,
    val accessToken: String,
    val tokenType: String,
    val refreshToken: String,
    val expiresIn: String,
    val scope: String
)