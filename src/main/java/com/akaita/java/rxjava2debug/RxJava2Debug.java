/*
 * Copyright 2017 akaita
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

package com.akaita.java.rxjava2debug;

import hu.akarnokd.rxjava2.debug.RxJavaAssemblyException;
import hu.akarnokd.rxjava2.debug.RxJavaAssemblyTracking;

import static com.akaita.java.rxjava2debug.ExceptionUtils.setRootCause;
import static com.akaita.java.rxjava2debug.StackTraceUtils.parseStackTrace;

public class RxJava2Debug {
    private static String BASE_APP_PACKAGE_NAME = "com.akaita.fgas";

    /**
     * Enable a system to collect information about RxJava's execution to provide a more meaningful StackTrace in case of crash<br/>
     * <b>Beware:</b> Any crash-reporting handler should be set up <i>before</i> calling this method
     */
    public static void enableRxJava2AssemblyTracking() {
        RxJavaAssemblyTracking.enable();
        setRxJavaAssemblyHandler();
    }

    /**
     * Set handler to intercept an exception, improve the StackTrace of RxJava-related ones and rethrow let the exception go through
     */
    private static void setRxJavaAssemblyHandler() {
        final Thread.UncaughtExceptionHandler previousDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Throwable toThrow = e;

                RxJavaAssemblyException assembledException = RxJavaAssemblyException.find(e);
                if (assembledException != null) {
                    StackTraceElement[] clearStack = parseStackTrace(assembledException, BASE_APP_PACKAGE_NAME);
                    Throwable clearException = new Throwable();
                    clearException.setStackTrace(clearStack);
                    toThrow = setRootCause(e, clearException);
                }

                previousDefaultHandler.uncaughtException(t, toThrow);
            }
        });
    }

}
