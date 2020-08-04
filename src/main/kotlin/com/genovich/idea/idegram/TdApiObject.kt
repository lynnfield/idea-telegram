package com.genovich.idea.idegram

import org.drinkless.tdlib.TdApi

sealed class TdApiObject {
    sealed class Update : TdApiObject() {
        data class AuthorizationState(val state: TdApiObject.AuthorizationState) : Update()
        data class NewChat(val chat: TdApi.Chat) : Update()
        data class Unknown(val apiObject: TdApi.Update) : Update()

        companion object {
            fun parse(apiObject: TdApi.Update): Update = when (apiObject) {
                is TdApi.UpdateAuthorizationState ->
                    AuthorizationState(
                        TdApiObject.AuthorizationState.parse(
                            apiObject.authorizationState
                        )
                    )
                is TdApi.UpdateNewChat -> NewChat(
                    apiObject.chat
                )
                else -> Unknown(apiObject)
            }
        }
    }

    sealed class AuthorizationState : TdApiObject() {
        object WaitTdlibParameters : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        object WaitPhoneNumber : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        data class WaitCode(val parse: AuthenticationCodeInfo) : AuthorizationState()
        data class WaitEncryptionKey(val encrypted: Boolean) : AuthorizationState()
        object Ready : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        object Closed : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        object Closing : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        object LoggingOut : AuthorizationState() {
            override fun toString(): String = this::class.qualifiedName.orEmpty()
        }

        data class WaitPassword(
            val passwordHint: String,
            val hasRecoveryEmailAddress: Boolean,
            val recoveryEmailAddressPattern: String
        ) : AuthorizationState()

        data class WaitRegistration(val termsOfService: TdApi.TermsOfService) : AuthorizationState()

        data class WaitOtherDeviceConfirmaiton(val link: String) : AuthorizationState()

        data class Unknown(val apiObject: TdApi.AuthorizationState) : AuthorizationState()

        companion object {
            fun parse(apiObject: TdApi.AuthorizationState): AuthorizationState = when (apiObject) {
                is TdApi.AuthorizationStateWaitTdlibParameters -> WaitTdlibParameters
                is TdApi.AuthorizationStateWaitPhoneNumber -> WaitPhoneNumber
                is TdApi.AuthorizationStateWaitCode ->
                    WaitCode(
                        AuthenticationCodeInfo.parse(
                            apiObject.codeInfo
                        )
                    )
                is TdApi.AuthorizationStateWaitEncryptionKey ->
                    WaitEncryptionKey(
                        apiObject.isEncrypted
                    )
                is TdApi.AuthorizationStateReady -> Ready
                is TdApi.AuthorizationStateClosed -> Closed
                is TdApi.AuthorizationStateClosing -> Closing
                is TdApi.AuthorizationStateLoggingOut -> LoggingOut
                is TdApi.AuthorizationStateWaitPassword -> WaitPassword(
                    apiObject.passwordHint,
                    apiObject.hasRecoveryEmailAddress,
                    apiObject.recoveryEmailAddressPattern
                )
                is TdApi.AuthorizationStateWaitRegistration -> WaitRegistration(
                    apiObject.termsOfService
                )
                is TdApi.AuthorizationStateWaitOtherDeviceConfirmation -> WaitOtherDeviceConfirmaiton(
                    apiObject.link
                )
                else -> Unknown(apiObject)
            }
        }
    }

    sealed class AuthenticationCodeType {
        data class Sms(val length: Int) : AuthenticationCodeType()
        data class Unknown(val apiObject: TdApi.AuthenticationCodeType) : AuthenticationCodeType()

        companion object {
            fun parse(apiObject: TdApi.AuthenticationCodeType): AuthenticationCodeType = when (apiObject) {
                is TdApi.AuthenticationCodeTypeSms -> Sms(
                    apiObject.length
                )
                else -> Unknown(
                    apiObject
                )
            }
        }
    }

    data class AuthenticationCodeInfo(
        val phoneNumber: String,
        val type: AuthenticationCodeType,
        val nextType: AuthenticationCodeType,
        val timeout: Int
    ) : TdApiObject() {
        companion object {
            fun parse(codeInfo: TdApi.AuthenticationCodeInfo): AuthenticationCodeInfo =
                AuthenticationCodeInfo(
                    codeInfo.phoneNumber.orEmpty(),
                    AuthenticationCodeType.parse(
                        codeInfo.type
                    ),
                    AuthenticationCodeType.parse(
                        codeInfo.nextType
                    ),
                    codeInfo.timeout
                )
        }
    }

    object Ok : TdApiObject() {
        override fun toString(): String = this::class.qualifiedName.orEmpty()
    }

    data class Error(val code: Int, val message: String) : TdApiObject()

    data class Chats(val chatIds: List<Long>) : TdApiObject()

    data class Messages(val messages: List<TdApi.Message>) : TdApiObject()

    data class Unknown(val apiObject: TdApi.Object) : TdApiObject()

    companion object {
        fun parse(apiObject: TdApi.Object): TdApiObject = when (apiObject) {
            is TdApi.Update -> Update.parse(
                apiObject
            )
            is TdApi.Chats -> Chats(apiObject.chatIds.toList())
            is TdApi.Ok -> Ok
            is TdApi.Error -> Error(
                apiObject.code,
                apiObject.message
            )
            is TdApi.Messages -> Messages(apiObject.messages.toList())
            else -> Unknown(apiObject)
        }
    }
}