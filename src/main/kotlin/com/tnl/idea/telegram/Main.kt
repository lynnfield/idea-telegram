package com.tnl.idea.telegram

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.panel
import cz.adamh.utils.NativeUtils
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JTextField

class Main : ToolWindowFactory {

    companion object {
        val logger = Logger.getLogger("telegram-idea")

        init {
            try {
                NativeUtils.loadLibraryFromJar("/libtdjni.dylib")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var client: Client
    private lateinit var toolWindow: ToolWindow

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.toolWindow = toolWindow
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val panel = panel {
            row { label("hello") }
        }
        val content = contentFactory.createContent(panel, "main", false)
        toolWindow.contentManager.addContent(content)

        logger.info("start")
        logger.info(File(".").absolutePath)

        client = Client.create(
            ::handleApiObject,
            { updatesException ->
                logger.log(Level.FINER, "updatesException", updatesException)
            },
            { defaultException ->
                logger.log(Level.FINER, "defaultException", defaultException)
            }
        )
    }

    private fun handleApiObject(apiObject: TdApi.Object) {
        logger.info("apiObject ${apiObject.constructor}")
        when (apiObject.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                val message = apiObject as TdApi.UpdateAuthorizationState
                when (message.authorizationState.constructor) {
                    TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                        val params = TdApi.TdlibParameters().apply {
                            databaseDirectory = "tdlib"
                            useMessageDatabase = true
                            useSecretChats = true
                            apiId = BuildConfig.APP_ID
                            apiHash = BuildConfig.API_HASH
                            systemLanguageCode = Locale.getDefault().language
                            deviceModel = "Desktop"
                            systemVersion = "Unknown"
                            applicationVersion = "0.1"
                            enableStorageOptimizer = true
                        }
                        client.send(TdApi.SetTdlibParameters(params), ::handleResponse)
                    }
                    TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                        replaceContent {
                            row { label("waiting for phone") }
                            val field = JTextField()
                            row { field() }
                            row {
                                button("submit") {
                                    client.send(TdApi.SetAuthenticationPhoneNumber(field.text, null), ::handleResponse)
                                }
                            }
                        }
                    }
                    TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                        replaceContent {
                            row { label("waiting for code") }
                            val field = JTextField()
                            row { field() }
                            row {
                                button("submit") {
                                    client.send(TdApi.CheckAuthenticationCode(field.text), ::handleResponse)
                                }
                            }
                        }
                    }
                    TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                        client.send(TdApi.CheckDatabaseEncryptionKey(), ::handleResponse)
                    }
                    TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                        replaceContent {
                            row { label("We are ready now!") }
                        }
                    }
                    else -> logger.log(
                        Level.INFO,
                        "not implemented auth state ${message.authorizationState.constructor}"
                    )
                }
            }
            else -> logger.info("not implemented apiObject ${apiObject.constructor}")
        }
    }

    private fun replaceContent(build: LayoutBuilder.() -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            toolWindow.contentManager.findContent("main")?.component?.removeAll()
            toolWindow.contentManager.findContent("main")?.component?.add(panel {
                build()
            })
            toolWindow.contentManager.findContent("main")?.component?.validate()
        }
    }

    private fun handleResponse(apiObject: TdApi.Object) {
        when (apiObject.constructor) {
            TdApi.Ok.CONSTRUCTOR -> logger.info("ok")
            TdApi.Error.CONSTRUCTOR -> logger.info("err $apiObject")
            else -> logger.info("wrong response ${apiObject.constructor}")
        }
    }
}