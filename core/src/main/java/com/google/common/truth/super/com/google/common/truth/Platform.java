/*
 * Copyright (c) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import static com.google.common.truth.StringUtil.format;

import com.google.common.truth.Truth.AssertionErrorWithCause;
import java.util.LinkedHashSet;
import java.util.Set;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Extracted routines that need to be swapped in for GWT, to allow for minimal deltas between the
 * GWT and non-GWT version.
 *
 * @author Christian Gruber (cgruber@google.com)
 */
final class Platform {
  private Platform() {}

  /** Returns true if the instance is assignable to the type Clazz. */
  static boolean isInstanceOfType(Object instance, Class<?> clazz) {
    return isInstanceOfTypeGWT(instance, clazz);
  }

  /**
   * Returns true if the instance is assignable to the type Clazz (though in GWT clazz can only be a
   * concrete class that is an ancestor class of the instance or the direct type of the instance.
   */
  static boolean isInstanceOfTypeGWT(Object instance, Class<?> clazz) {
    String className = clazz.getName();
    Set<String> types = new LinkedHashSet<String>();
    addTypeNames(instance.getClass(), types);
    return types.contains(className);
  }

  private static void addTypeNames(Class<?> clazz, Set<String> types) {
    for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
      types.add(current.getName());
      // addInterfaceNames(current.getInterfaces(), types);
    }
  }

  static AssertionError comparisonFailure(
      String message, String expected, String actual, Throwable cause) {
    throw new AssertionErrorWithCause(
        format("%s expected:<[%s]> but was:<[%s]>", message, expected, actual), cause);
  }

  /** Determines if the given subject contains a match for the given regex. */
  static boolean containsMatch(String subject, String regex) {
    return compile(regex).test(subject);
  }

  /**
   * Returns an array containing all of the exceptions that were suppressed to deliver the given
   * exception. Delegates to the getSuppressed() method on Throwable that is available in Java 1.7+
   */
  static Throwable[] getSuppressed(Throwable throwable) {
    return throwable.getSuppressed();
  }

  /** Always returns false. Stack traces will be cleaned by default. */
  static boolean isStackTraceCleaningDisabled() {
    return false;
  }

  /** Tests if current platform is Android which is always false. */
  static boolean isAndroid() {
    return false;
  }

  /** Returns a human readable string representation of the throwable's stack trace. */
  static String getStackTraceAsString(Throwable throwable) {
    // TODO(cpovirk): Write a naive implementation that at least dumps the main exception's stack.
    return throwable.toString();
  }

  /**
   * A GWT-swapped version of test rule interface that does nothing. All methods extended from
   * {@link org.junit.rules.TestRule} needs to be stripped.
   */
  interface JUnitTestRule {}

  // TODO(user): Move this logic to a common location.
  private static NativeRegExp compile(String pattern) {
    return new NativeRegExp(pattern);
  }

  @JsType(isNative = true, name = "RegExp", namespace = JsPackage.GLOBAL)
  private static class NativeRegExp {
    public NativeRegExp(String pattern) {}

    public native boolean test(String input);
  }
}
