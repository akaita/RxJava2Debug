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

import io.reactivex.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;

class ExceptionUtils {

    static Throwable setRootCause(@NonNull Throwable throwable, @NonNull Throwable rootCause) {
        List<Throwable> causes = listCauses(throwable);
        causes.add(rootCause);
        return reverseAndCollapseCauses(causes);
    }

    @NonNull
    private static List<Throwable> listCauses(@NonNull Throwable throwable) {
        LinkedList<Throwable> causes = new LinkedList<Throwable>();
        Throwable cause = throwable.getCause();
        while (cause != null && !causes.contains(cause)) {
            causes.add(cause);
            cause = cause.getCause();
        }
        return causes;
    }

    @NonNull
    private static Throwable reverseAndCollapseCauses(@NonNull List<Throwable> causes) {
        if (causes.size() == 0) {
            return new RuntimeException("Empty list of causes");
        }

        String originalEnhancedMessage = causes.get(causes.size() - 1).getLocalizedMessage();
        String topMessage = "caused by " + originalEnhancedMessage;

        Throwable topThrowable = null;
        for (int i = causes.size() - 1; i >= 0; i--) {
            if (i > 0) {
                topThrowable = new Throwable(causes.get(i).getMessage(), topThrowable);
            } else {
                topThrowable = new Throwable(topMessage, topThrowable);
            }

            if (causes.get(i).getStackTrace() != null) {
                // This array should never be null, if everybody follows the Java spec
                // Sometimes this part of the spec is not followed, so we better protect ourselves
                topThrowable.setStackTrace(causes.get(i).getStackTrace());
            }
        }

        return topThrowable;
    }
}
