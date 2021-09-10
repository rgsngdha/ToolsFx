package me.leon.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import me.leon.encode.base.base64
import me.leon.ext.DEFAULT_SPACING
import me.leon.ext.EncodeType
import me.leon.ext.clipboardText
import me.leon.ext.copy
import me.leon.ext.fileDraggedHandler
import me.leon.ext.readBytesFromNet
import me.leon.ext.readFromNet
import me.leon.ext.readHeadersFromNet
import tornadofx.FX.Companion.messages
import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.checkbox
import tornadofx.contextmenu
import tornadofx.get
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.item
import tornadofx.label
import tornadofx.paddingAll
import tornadofx.paddingBottom
import tornadofx.paddingTop
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.vbox

class StringProcessView : View(messages["stringProcess"]) {
    override val closeable = SimpleBooleanProperty(false)
    private val isRegexp = SimpleBooleanProperty(false)
    private val isSplitRegexp = SimpleBooleanProperty(false)
    private lateinit var taInput: TextArea
    private lateinit var taOutput: TextArea
    private lateinit var tfReplaceFrom: TextField
    private var replaceFromText
        get() = tfReplaceFrom.text.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
        set(value) {
            tfReplaceFrom.text = value
        }

    private lateinit var tfReplaceTo: TextField
    private var replaceToText
        get() = tfReplaceTo.text.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
        set(value) {
            tfReplaceTo.text = value
        }
    private lateinit var tfSplitLength: TextField
    private var splitLengthText
        get() =
            runCatching { tfSplitLength.text.toInt() }.getOrElse {
                tfSplitLength.text = "8"
                8
            }
        set(value) {
            tfSplitLength.text = value.toString()
        }

    private lateinit var tfSeprator: TextField
    private var sepratorText
        get() =
            tfSeprator
                .text
                .also { println(it) }
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .also { println("__${it}___") }
        set(value) {
            tfSeprator.text = value
        }

    private lateinit var labelInfo: Label
    private val info: String
        get() =
            " ${messages["inputLength"]}:" +
                " ${inputText.length}  ${messages["outputLength"]}: ${outputText.length}"
    private var inputText: String
        get() =
            taInput.text.takeIf {
                isEncode || encodeType in arrayOf(EncodeType.Decimal, EncodeType.Octal)
            }
                ?: taInput.text.replace("\\s".toRegex(), "")
        set(value) {
            taInput.text = value
        }
    private var outputText: String
        get() = taOutput.text
        set(value) {
            taOutput.text = value
        }

    private var encodeType = EncodeType.Base64
    private var isEncode = true

    private val eventHandler = fileDraggedHandler { taInput.text = it.first().readText() }

    private val centerNode = vbox {
        paddingAll = DEFAULT_SPACING
        spacing = DEFAULT_SPACING
        hbox {
            label(messages["input"])
            spacing = DEFAULT_SPACING
            button(graphic = imageview("/import.png")) { action { taInput.text = clipboardText() } }
            button(graphic = imageview("/uppercase.png")) {
                action { outputText = inputText.uppercase() }
            }
            button(graphic = imageview("/lowercase.png")) {
                action { outputText = inputText.lowercase() }
            }
            button(graphic = imageview("/ascend.png")) {
                action {
                    outputText =
                        inputText
                            .split("\n|\r\n".toRegex())
                            .sorted()
                            .joinToString(System.lineSeparator())
                }
            }
            button(graphic = imageview("/descend.png")) {
                action {
                    outputText =
                        inputText
                            .split("\n|\r\n".toRegex())
                            .sortedDescending()
                            .joinToString(System.lineSeparator())
                }
            }
            button(graphic = imageview("/statisc.png")) {
                action {
                    outputText =
                        inputText
                            .fold(mutableMapOf<Char, Int>()) { acc, c ->
                                acc.apply { acc[c] = (acc[c] ?: 0) + 1 }
                            }
                            .toList()
                            .joinToString(System.lineSeparator()) { "${it.first}: ${it.second}" }
                }
            }
        }

        taInput =
            textarea {
                promptText = messages["inputHint"]
                isWrapText = true
                onDragEntered = eventHandler
                contextmenu {
                    item(messages["loadFromNet"]) {
                        action { runAsync { inputText.readFromNet() } ui { taInput.text = it } }
                    }
                    item(messages["loadFromNet2"]) {
                        action {
                            runAsync { inputText.readBytesFromNet().base64() } ui
                                {
                                    taInput.text = it
                                }
                        }
                    }
                    item(messages["readHeadersFromNet"]) {
                        action {
                            runAsync { inputText.readHeadersFromNet() } ui { taInput.text = it }
                        }
                    }
                }
            }
        hbox {
            alignment = Pos.CENTER_LEFT
            paddingTop = DEFAULT_SPACING
            paddingBottom = DEFAULT_SPACING
            spacing = DEFAULT_SPACING
            label(messages["replace"])
            tfReplaceFrom = textfield { promptText = messages["text2Replaced"] }
            tfReplaceTo = textfield { promptText = messages["replaced"] }
            checkbox(messages["regexp"], isRegexp)
            button(messages["run"], imageview("/run.png")) { action { doReplace() } }
        }
        hbox {
            alignment = Pos.CENTER_LEFT
            paddingTop = DEFAULT_SPACING
            paddingBottom = DEFAULT_SPACING
            spacing = DEFAULT_SPACING
            label(messages["split"])
            tfSplitLength = textfield { promptText = messages["splitLength"] }
            tfSeprator = textfield { promptText = messages["seprator"] }
            checkbox(messages["regexp"], isSplitRegexp) { isVisible = false }
            button(messages["run"], imageview("/run.png")) { action { doSplit() } }
        }

        hbox {
            spacing = DEFAULT_SPACING
            label(messages["output"])
            button(graphic = imageview("/copy.png")) { action { outputText.copy() } }
            button(graphic = imageview("/up.png")) {
                action {
                    taInput.text = outputText
                    taOutput.text = ""
                }
            }
        }

        taOutput =
            textarea {
                promptText = messages["outputHint"]
                isWrapText = true
            }
    }

    private fun doSplit() {
        outputText =
            inputText.toList().chunked(splitLengthText).joinToString(sepratorText) {
                it.joinToString("")
            }
        labelInfo.text = info
    }

    override val root = borderpane {
        center = centerNode
        bottom = hbox { labelInfo = label(info) }
    }

    private fun doReplace() {
        if (replaceFromText.isNotEmpty()) {
            println(replaceToText)
            outputText =
                if (isRegexp.get()) inputText.replace(replaceFromText.toRegex(), replaceToText)
                else inputText.replace(replaceFromText, replaceToText)
        }
        labelInfo.text = info
    }
}
