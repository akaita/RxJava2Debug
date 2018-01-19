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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import com.akaita.java.rxjava2debug.RxJava2Debug
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleAssembly.setOnCheckedChangeListener({ _, isChecked -> toggleRxJava2Debug(isChecked) })
        handledException.setOnClickListener { generateHandledException() }
        unhandledException.setOnClickListener { generateUnhandledException() }
    }

    /**
     * Toggle RxJava2Debug on and off.
     */
    private fun toggleRxJava2Debug( enable: Boolean ) {
        if (enable) {
            RxJava2Debug.enableRxJava2AssemblyTracking(ExampleApplication.MY_CODE_PACKAGES)
        } else {
            RxJava2Debug.disableRxJava2AssemblyTracking()
        }
    }

    /**
     * Generates an exception in a computation thread, by inserting a `null` event into a stream
     *
     * By handling this exception in `onError`, it prevents the app from crashing.
     *
     * It handles the exception by printing it in LogCat and the screen, so the developer can fix it
     */
    private fun generateHandledException() {
        Single.just("event")
                .subscribeOn(Schedulers.computation())
                .doOnEvent { _, _ ->  Log.i("HandledException", "Start") }
                .map { null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { Log.i("HandledException", "Subscribe") },
                        { t: Throwable ->
                            val enhancedStackTrace = RxJava2Debug.getEnhancedStackTrace(t)
                            Log.e("HandledException", "Error", enhancedStackTrace)
                            displayStackTrace(enhancedStackTrace)
                        }
                )
    }

    /**
     * Generates an exception in a computation thread, by inserting a `null` event into a stream
     *
     * No implementation for `onError` is provided, so the exception will be thrown and it will eventually crash the app O_o
     */
    private fun generateUnhandledException() {
        Single.just("event")
                .subscribeOn(Schedulers.computation())
                .doOnEvent { _, _ ->  Log.i("HandledException", "Start") }
                .map { null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { Log.i("UnhandledException", "Subscribe") })
    }

    /**
     * Obtain an HTML version of the stacktrace, so it looks nice on the screen
     */
    private fun displayStackTrace(throwable: Throwable?) {
        textView.text = Html.fromHtml(
                throwable
                        ?.toHtml()
                        ?.highlight(ExampleApplication.MY_CODE_PACKAGES) ?: "Something horrible happened!") //
    }
}
