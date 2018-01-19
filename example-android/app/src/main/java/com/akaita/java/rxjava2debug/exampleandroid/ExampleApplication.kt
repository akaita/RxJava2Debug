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

import android.app.Application
import android.util.Log
import com.akaita.java.rxjava2debug.RxJava2Debug

/**
 * It is good practice to enable RxJava2Debug in the app's Application class, so it is enabled as soon as possible
 */
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        enableCrashlytics()

        // Enable RxJava2Debug in Application
        // Make sure it is enabled RIGHT AFTER setting up any Crash reporting mechanism such as Crashlytics
        // Provide a list of the root packages of your code, so RxJava2Debug can point you to the source of the stream
        RxJava2Debug.enableRxJava2AssemblyTracking(MY_CODE_PACKAGES)
    }

    /**
     * This method just replicates Crashlytics' behaviour:
     *
     * It sets up the same system that Crashlytics does, but instead of sending the report to Fabric it will print it to LogCat
     */
    private fun enableCrashlytics() {
        val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("UnhandledException", "Error", throwable)
            defaultUncaughtHandler.uncaughtException(thread, throwable)
        }
    }


    companion object {

        /**
         * This array assumes that the code I wrote for this app is contained under three root packages
         *
         * This specific example only needs "com.akaita.java.rxjava2debug.exampleandroid". I just added two more in order to show how you could do it
         */
        val MY_CODE_PACKAGES = arrayOf(
                "com.akaita.java.rxjava2debug.exampleandroid",
                "com.akaita.java.rxjava2debug.module1",
                "com.akaita.java.rxjava2debug.module2"
        )
    }
}
