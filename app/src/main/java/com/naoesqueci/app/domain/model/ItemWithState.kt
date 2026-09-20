package com.naoesqueci.app.domain.model

data class ItemWithState(
    val item: TripItem,
    val checkState: CheckState?
)