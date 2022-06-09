package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.io.newFile
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal fun compileFile(file: File, sender: ProxyCommandSender, props: Map<String, Any>, info: Boolean = true): ScriptCompiled? {
    return if (file.extension == "kts") {
        val time = System.currentTimeMillis()
        val future = CompletableFuture<Void>()
        val platformTask = submit(async = true, period = 20, delay = 20) {
            // > 1s
            val seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
            if (seconds > 0) {
                sender.sendLang("command-script-compile-wait", seconds)
            }
        }
        // > Compiling...
        // > Provided-Properties: {}
        if (info) {
            sender.sendLang("command-script-compile-info", props)
        }
        future.thenAccept { platformTask.cancel() }
        Artifex.api().getScriptCompiler().compile {
            it.source(file)
            it.configuration(ScriptRuntimeProperty().also { property ->
                property.providedProperties.putAll(props)
            })
            it.onReport { r -> reportResult(r, sender) }
            it.onSuccess { script ->
                future.complete(null)
                sender.sendLang("command-script-compile-successful", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                // 释放构建文件
                script.generateScriptJar(newFile(scriptsFile, ".build/${file.nameWithoutExtension}.jar"))
            }
            it.onFailure {
                future.complete(null)
                sender.sendLang("command-script-compile-failed", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
            }
        }
    } else null
}

internal fun compileScript(source: String, sender: ProxyCommandSender): CompletableFuture<ScriptCompiled?> {
    val time = System.currentTimeMillis()
    val future = CompletableFuture<ScriptCompiled?>()
    val platformTask = submit(async = true, period = 20, delay = 20) {
        // > 1s
        val seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
        if (seconds > 0) {
            sender.sendLang("command-script-compile-wait", seconds)
        }
    }
    sender.sendLang("command-script-shell-compile")
    future.thenAccept { platformTask.cancel() }
    Artifex.api().getScriptCompiler().compile {
        it.source("main", source)
        it.onReport { r -> reportResult(r, sender) }
        it.onSuccess { r ->
            future.complete(r)
            sender.sendLang("command-script-compile-successful", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
        }
        it.onFailure {
            future.complete(null)
            sender.sendLang("command-script-compile-failed", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
        }
    }
    return future
}