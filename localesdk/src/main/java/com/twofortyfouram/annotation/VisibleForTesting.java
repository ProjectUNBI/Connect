/*
 * android-annotation-lib https://github.com/twofortyfouram/android-annotation
 * Copyright 2014 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twofortyfouram.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Documents code that has a higher visibility for testing purposes.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface VisibleForTesting {

    /**
     * Documents the intended visibility.
     */
    public static enum Visibility {
        /**
         * The visibility is intended to be public.
         */
        PUBLIC,

        /**
         * The visibility is intended to be package.
         */
        PACKAGE,

        /**
         * The visibility is intended to be protected.
         */
        PROTECTED,

        /**
         * The visibility is intended to be private.
         */
        PRIVATE,

        /**
         * The code's sole purpose is for testing and wouldn't exist otherwise.
         * <p>
         * For example, some objects may expose a test constructor to make
         * injecting special values easier at test time.
         */
        TEST
    }

    Visibility value();
}