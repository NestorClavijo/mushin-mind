package com.mushind.mind.domain.model

data class UserProgress(
    val balance: Int = 0,
    val xp: Int = 0,
) {
    init {
        require(balance >= 0) { "Balance cannot be negative" }
        require(xp >= 0) { "XP cannot be negative" }
    }
}

