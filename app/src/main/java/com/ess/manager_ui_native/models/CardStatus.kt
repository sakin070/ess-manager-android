package com.ess.manager_ui_native.models

enum class CardStatus {
    CREATED, ASSIGNED, SOLD, SETTLED, INVALID;

    companion object {
        fun fromInt(int: Int): CardStatus {
            values().forEach { cardStatus -> if (cardStatus.ordinal == int) return cardStatus }
            return INVALID
        }
    }
}