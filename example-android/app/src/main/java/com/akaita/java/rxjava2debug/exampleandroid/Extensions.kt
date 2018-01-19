/*
 * Copyright 2018 akaita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akaita.java.rxjava2debug.exampleandroid

/**
 * You don't care about this file.
 * This file just contains utilities for the sake of UI prettiness, nothing directly  related to the usage of RxJava2Debug
 */

fun Throwable.toHtml(): String {
    val sb = StringBuilder()

    var t: Throwable? = this
            .let {
                sb.append("<font color=\"red\">${it.javaClass.canonicalName}: ${it.message}</font><br>")
                sb.append(it.stackTrace.toHtml())
                it.cause
            }

    while (t != null) {
        sb.append("Caused by: ${t.javaClass}<br>")
        sb.append(t.stackTrace.toHtml())
        t = t.cause
    }

    return sb.toString()
}

fun Array<StackTraceElement>.toHtml(): String {
    val sb = StringBuilder()

    this.forEach { element ->
        sb.append("\tat $element<br>")
    }

    return sb.toString()
}

fun String.highlight(patterns: Array<String>): String {
    var result = this
    patterns.forEach { pattern ->
                result = result.highlight(pattern)
            }
    return result
}

fun String.highlight(pattern: String): String {
    val sb = StringBuilder()
    this.split("<br>")
            .forEach { line ->
                if (line.contains(pattern)) {
                    sb.append("<font color=\"blue\">$line</font><br>")
                } else {
                    sb.append("$line<br>")
                }
            }
    return sb.toString()
}