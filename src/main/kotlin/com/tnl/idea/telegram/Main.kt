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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JTextField

class Main : ToolWindowFactory {

    companion object {
        val logger: Logger = Logger.getLogger("com.tnl.idea.telegram")

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
    private val chats = CopyOnWriteArrayList<TdApi.Chat>()

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

        when (val tdApiObject = TdApiObject.parse(apiObject).also { logger.info(it.toString()) }) {
            is TdApiObject.Update -> when (tdApiObject) {
                is TdApiObject.Update.AuthorizationState -> when (tdApiObject.state) {
                    TdApiObject.AuthorizationState.WaitTdlibParameters -> client.send(
                        TdApi.SetTdlibParameters(
                            TdApi.TdlibParameters().apply {
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
                        ),
                        ::handleResponse
                    )
                    TdApiObject.AuthorizationState.WaitPhoneNumber -> replaceContent {
                        row { label("waiting for phone") }
                        val field = JTextField()
                        row { field() }
                        row {
                            button("submit") {
                                client.send(TdApi.SetAuthenticationPhoneNumber(field.text, null), ::handleResponse)
                            }
                        }
                    }
                    is TdApiObject.AuthorizationState.WaitCode -> replaceContent {
                        row { label("waiting for code") }
                        val field = JTextField()
                        row { field() }
                        row {
                            button("submit") {
                                client.send(TdApi.CheckAuthenticationCode(field.text), ::handleResponse)
                            }
                        }
                    }
                    is TdApiObject.AuthorizationState.WaitEncryptionKey -> client.send(
                        TdApi.CheckDatabaseEncryptionKey(),
                        ::handleResponse
                    )
                    TdApiObject.AuthorizationState.Ready -> {
                        replaceContent { row { label("loading chat list") } }
                        client.send(TdApi.GetChats(TdApi.ChatListMain(), Long.MAX_VALUE, 0, 10)) {
                            when (val response = TdApiObject.parse(it)) {
                                is TdApiObject.Chats -> logger.info(response.chatIds.joinToString())
                                else -> logger.info("unexpected GetChats response: $response")
                            }
                        }
                    }
                    is TdApiObject.AuthorizationState.Unknown -> logger.info(tdApiObject.state.toString())
                }
                is TdApiObject.Update.NewChat -> {
                    chats.add(tdApiObject.chat)
                    val currentSize = chats.size
                    replaceContent(
                        build = {
                            chats.forEach { chat ->
                                row { label(chat.title) }
                            }
                        },
                        predicate = {
                            currentSize != chats.size
                        }
                    )
                }
                is TdApiObject.Update.Unknown -> logger.info(tdApiObject.toString())
            }
            else -> logger.info(tdApiObject.toString())
        }
    }

    private fun replaceContent(build: LayoutBuilder.() -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            toolWindow.contentManager.findContent("main")?.component?.apply {
                removeAll()
                add(panel { build() })
                validate()
            }
        }
    }

    private fun replaceContent(build: LayoutBuilder.() -> Unit, predicate: () -> Boolean) {
        ApplicationManager.getApplication().invokeLater(
            {
                toolWindow.contentManager.findContent("main")?.component?.apply {
                    removeAll()
                    add(panel { build() })
                    validate()
                }
            },
            { predicate() }
        )
    }

    private fun handleResponse(apiObject: TdApi.Object) {
        logger.info(
            when (val tdApiObject = TdApiObject.parse(apiObject)) {
                is TdApiObject.Ok, is TdApiObject.Error -> tdApiObject.toString()
                else -> "unexpected response $tdApiObject"
            }
        )
    }
}