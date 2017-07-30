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
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StackTraceUtils {
    //Example: "at com.akaita.fgas.MainActivity.onCreate(MainActivity.java:210)"
    private static final Pattern STACK_TRACE_ELEMENT_PATTERN = Pattern.compile("^at (.*)\\.(.*)\\((.*):([0-9]+)\\)$");
    private static final String NEW_LINE_REGEX = "\\n\\r|\\r\\n|\\n|\\r";

    /**
     * Extract StackTrace and filter to show an app-specific entry at its top
     *
     * @param exception RxJavaAssemblyException to be parsed
     * @return StackTrace, filtered so a app-specific line is at the top of it
     */
    @NonNull
    static StackTraceElement[] parseStackTrace(@NonNull RxJavaAssemblyException exception, @Nullable String[] basePackages) {
        String[] lines = exception.stacktrace()
                .split(NEW_LINE_REGEX);

        List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();
        boolean filterIn = false;
        for (String line : lines) {
            filterIn = filterIn
                    || basePackages == null
                    || basePackages.length == 0
                    || startsWithAny(line, basePackages);
            if (filterIn) {
                StackTraceElement element = parseStackTraceLine(line);
                if (element != null) {
                    stackTrace.add(element);
                }
            }
        }

        return stackTrace.toArray(new StackTraceElement[0]);
    }

    static boolean startsWithAny(@NonNull String input, @NonNull String[] matchers) {
        for (String matcher :
                matchers) {
            if (input.startsWith("at "+matcher) || input.startsWith(matcher)) return true;
        }
        return false;
    }

    /**
     * Parse string containing a <i>single line</i> of a StackTrace
     *
     * @param stackTraceLine string containing single line of a StackTrace
     * @return parsed StackTraceElement
     */
    @Nullable
    private static StackTraceElement parseStackTraceLine(@NonNull String stackTraceLine) {
        StackTraceElement retVal = null;

        Matcher matcher = STACK_TRACE_ELEMENT_PATTERN.matcher(stackTraceLine);
        if (matcher.matches()) {
            String clazz = matcher.group(1);
            String method = matcher.group(2);
            String filename = matcher.group(3);
            int line = Integer.valueOf(matcher.group(4));
            retVal = new StackTraceElement(clazz, method, filename, line);
        }
        return retVal;
    }
}
