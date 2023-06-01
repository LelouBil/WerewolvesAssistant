package net.leloubil.common

import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


class CustomAntiLog : Antilog() {
    companion object{
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        @ExperimentalTerminalApi
        val t = Terminal()
        const val CALL_STACK_INDEX = 8
        val anonymousClass: Pattern = Pattern.compile("(\\$\\d+)+$")
    }
    private fun createStackElementTag(className: String): String {
        var tag = className
        val m = anonymousClass.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }

    private fun performTag(): String?{
        val thread = Thread.currentThread().stackTrace

        return if (thread.size >= CALL_STACK_INDEX) {
            thread[CALL_STACK_INDEX].run {
                "${createStackElementTag(className)}\$$methodName"
            }
        } else {
            null
        }
    }


    @ExperimentalTerminalApi
    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        val formattedTime = timeFormatter.format(LocalTime.now())
        val msgColor = when(priority){
            LogLevel.DEBUG -> green
            LogLevel.INFO -> blue
            LogLevel.WARNING -> yellow
            LogLevel.ERROR -> red
            LogLevel.ASSERT -> red
            LogLevel.VERBOSE -> blue
        }

        val actualTag = tag ?: performTag()

        t.println("${white(formattedTime)} ${msgColor("[$priority]${if (actualTag != null) " $actualTag - " else " "}$message")}")
        if(throwable != null){
            t.println(red(throwable.stackTraceToString()))
        }
    }
}
