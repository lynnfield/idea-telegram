package com.tnl.idea.telegram

import org.drinkless.tdlib.TdApi

sealed class TdApiObject {
    sealed class Update : TdApiObject() {
        data class AuthorizationState(val state: TdApiObject.AuthorizationState) : Update()
        data class Unknown(val apiObject: TdApi.Update) : Update()

        companion object {
            fun parse(apiObject: TdApi.Update): Update = when (apiObject) {
                is TdApi.UpdateAuthorizationState -> AuthorizationState(
                    TdApiObject.AuthorizationState.parse(apiObject.authorizationState))
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
        data class Unknown(val apiObject: TdApi.AuthorizationState) : AuthorizationState()

        companion object {
            fun parse(apiObject: TdApi.AuthorizationState): AuthorizationState = when (apiObject) {
                is TdApi.AuthorizationStateWaitTdlibParameters -> WaitTdlibParameters
                is TdApi.AuthorizationStateWaitPhoneNumber -> WaitPhoneNumber
                is TdApi.AuthorizationStateWaitCode -> WaitCode(AuthenticationCodeInfo.parse(apiObject.codeInfo))
                is TdApi.AuthorizationStateWaitEncryptionKey -> WaitEncryptionKey(apiObject.isEncrypted)
                is TdApi.AuthorizationStateReady -> Ready
                else -> Unknown(apiObject)
            }
        }
    }

    sealed class AuthenticationCodeType {
        data class Sms(val length: Int) : AuthenticationCodeType()
        data class Unknown(val apiObject: TdApi.AuthenticationCodeType) : AuthenticationCodeType()

        companion object {
            fun parse(apiObject: TdApi.AuthenticationCodeType): AuthenticationCodeType = when (apiObject) {
                is TdApi.AuthenticationCodeTypeSms -> Sms(apiObject.length)
                else -> Unknown(apiObject)
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
            fun parse(codeInfo: TdApi.AuthenticationCodeInfo): AuthenticationCodeInfo = AuthenticationCodeInfo(
                codeInfo.phoneNumber.orEmpty(),
                AuthenticationCodeType.parse(codeInfo.type),
                AuthenticationCodeType.parse(codeInfo.nextType),
                codeInfo.timeout
            )
        }
    }

    object Ok : TdApiObject() {
        override fun toString(): String = this::class.qualifiedName.orEmpty()
    }
    data class Error(val code: Int, val message: String) : TdApiObject()

    data class Unknown(val apiObject: TdApi.Object) : TdApiObject()

    companion object {
        fun parse(apiObject: TdApi.Object): TdApiObject = when (apiObject) {
            is TdApi.Update -> Update.parse(apiObject)
            is TdApi.Ok -> Ok
            is TdApi.Error -> Error(apiObject.code, apiObject.message)
            else -> Unknown(apiObject)
        }
    }
}