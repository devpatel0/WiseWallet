package com.mobileapp.wisewallet.ui.home

sealed interface AddEvent {
    object SaveAccount: AddEvent
    data class SetName(val name: String): AddEvent
    data class SetType(val type: String): AddEvent
    data class SetBalance(val balance: Long): AddEvent

    object ShowDialog: AddEvent
    object HideDialog: AddEvent

}