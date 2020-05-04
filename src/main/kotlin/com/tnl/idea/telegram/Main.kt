package com.tnl.idea.telegram

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.layout.panel
import java.io.File

class Main : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val file = File(project.basePath, "src/main/kotlin/com/tnl/idea/telegram/Main.kt")
        val panel = panel {
            row { label("Hello world") }
            row { label("${project.basePath}") }
            row { label("$file") }
            row {
                button("open file") {
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)?.also { virtualFile ->
                        OpenFileDescriptor(project, virtualFile).navigate(true)
                    }
                }
            }
        }
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}