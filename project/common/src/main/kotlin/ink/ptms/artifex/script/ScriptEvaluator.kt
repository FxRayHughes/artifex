package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptEvaluator
 *
 * @author 坏黑
 * @since 2022/5/16 00:44
 */
interface ScriptEvaluator {

    /**
     * 创建运行配置
     *
     * @param id 脚本序号
     * @param props 脚本运行参数
     */
    fun createEvaluationConfiguration(id: String, props: ScriptRuntimeProperty): Configuration

    /**
     * 运行脚本文件，
     * 当满足以下条件时重新编译源代码：
     * 1. 文件被修改
     * 2. providedProperties 中属性的数量、名称或类型被修改
     *
     * @param sender 日志接收者
     * @param option 选项
     */
    fun eval(file: File, sender: ProxyCommandSender, option: Option): CompletableFuture<ScriptResult<ScriptResult.Result>>

    /**
     * 脚本运行选项
     */
    interface Option

    /**
     * 脚本运行配置
     */
    interface Configuration
}