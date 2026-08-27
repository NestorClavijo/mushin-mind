package com.mushind.mind.core.common

import java.util.UUID

fun interface IdProvider {
    fun newId(): String
}

class UuidProvider : IdProvider {
    override fun newId(): String = UUID.randomUUID().toString()
}

