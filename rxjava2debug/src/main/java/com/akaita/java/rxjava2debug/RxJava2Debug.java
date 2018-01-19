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
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

import java.util.List;

import static com.akaita.java.rxjava2debug.ExceptionUtils.setRootCause;
import static com.akaita.java.rxjava2debug.StackTraceUtils.parseStackTrace;

public class RxJava2Debug {
    private @Nullable
    static String[] basePackages;

    /**
     * Start collecting information about RxJava's execution to provide a more meaningful StackTrace in case of crash<br/>
     * <b>Beware:</b> Any crash-reporting handler should be set up <i>before</i> calling this method
     */
    public static void enableRxJava2AssemblyTracking() {
        enableRxJava2AssemblyTracking(null);
    }

    /**
     * Start collecting filtered information about RxJava's execution to provide a more meaningful StackTrace in case of crash<br/>
     * <b>Beware:</b> Any crash-reporting handler should be set up <i>before</i> calling this method
     * @param basePackageNames List of base package names of your code, so the created stacktrace will have one of those on its top<br/>
     *                         <i>null</i> to disable any filtering
     */
    public static void enableRxJava2AssemblyTracking(@Nullable String[] basePackageNames) {
        basePackages = basePackageNames;
        RxJavaAssemblyTracking.enable();
        setRxJavaAssemblyHandler();
    }

    /**
     * Disable the collection of more information about RxJava's execution<br/>
     * Information collected before calling this method will still be reported
     */
    public static void disableRxJava2AssemblyTracking() {
        RxJavaAssemblyTracking.disable();
    }

    /**
     * Obtain a copy of the original Throwable with an extended StackTrace
     * @param original Original Throwable
     * @return new Throwable with enhanced StackTrace if information was found. <i>Original Throwable</i> otherwise
     */
    public static @NonNull Throwable getEnhancedStackTrace(@NonNull Throwable original) {
        Throwable enhanced = original;

        RxJavaAssemblyException assembledException = RxJavaAssemblyException.find(original);
        if (assembledException != null) {
            StackTraceElement[] clearStack = parseStackTrace(assembledException, basePackages);
            Throwable clearException = new Throwable();
            clearException.setStackTrace(clearStack);
            enhanced = setRootCause(original, clearException);
        }

        return enhanced;
    }

    /**
     * Set handler to intercept an exception, improve the StackTrace of RxJava-related ones and rethrow let the exception go through
     */
    private static void setRxJavaAssemblyHandler() {
        final Thread.UncaughtExceptionHandler previousDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Throwable enhancedStackTrace = getEnhancedStackTrace(e);
                previousDefaultHandler.uncaughtException(t, enhancedStackTrace);
            }
        });
    }

}
