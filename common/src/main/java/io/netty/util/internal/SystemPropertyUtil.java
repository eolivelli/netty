/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A collection of utility methods to retrieve and parse the values of the Java system properties.
 * This library takes into account the fact that Netty classes are usually repackaged and relocated.
 */
public final class SystemPropertyUtil {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);

    /**
     * Maven shade plugin rewrites somehow string contrants
     * We want to construct a string that is not subject to this transformations.
     *
     * @param components the strings to join
     * @return a new string
     */
    private static String composeUnrelocatableString(String ... components) {
        StringBuilder res = new StringBuilder();
        for (String s : components) {
            res.append(s);
        }
        return res.toString();
    }

    static {
        detectRelocation(
            composeUnrelocatableString("io.","netty.","util.internal.SystemPropertyUtil"),
            SystemPropertyUtil.class.getName());
    }
    /**
     * Try to detect that Netty has been relocated to a different package name.
     * When you are relocating Netty classes you want an isolated version of Netty
     * with its own set of settings.
     */
    static void detectRelocation(String originalName, final String currentName) {
        if (originalName.equals(currentName)) {
            return;
        }
        logger.info("Detected a relocated version of netty, "
                    + "'{}' has been rewritten to '{}' will be prepended to every system property for this instance of Netty."
                    + "The final result depends on the utility used to repackage netty."
                    + "See the logs in order to see the actual name of system properties", originalName, currentName);
    }

    /**
     * This is a dummy method that echoes the passed value.
     * Relocation tools rewrite all of the string constants
     * and so we won't see here the original io.netty.xxx
     * property names but those names after the transformation.
     *
     * @param key
     *
     * @return the same key
     * @see #detectRelocation(java.lang.String, java.lang.String)
     */
    public static String propertyName(String key) {
        return key;
    }

    /**
     * Returns {@code true} if and only if the system property with the specified {@code key}
     * exists.
     */
    public static boolean contains(String key) {
        return get(key) != null;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to {@code null} if the property access fails.
     *
     * @return the property value or {@code null}
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     * @param key the name of the property.
     * @param def the default value.
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static String get(final String key, final String def) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key must not be empty.");
        }
        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty(key);
                    }
                });
            }
        } catch (SecurityException e) {
            logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
        }

        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static boolean getBoolean(String key, boolean def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            return def;
        }

        if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
            return true;
        }

        if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
            return false;
        }

        logger.warn(
                "Unable to parse the boolean system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static int getInt(String key, int def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            // Ignore
        }

        logger.warn(
                "Unable to parse the integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    /**
     * Returns the value of the Java system property with the specified
     * {@code key}, while falling back to the specified default value if
     * the property access fails.
     *
     * @return the property value.
     *         {@code def} if there's no such property or if an access to the
     *         specified property is not allowed.
     */
    public static long getLong(String key, long def) {
        String value = get(key);
        if (value == null) {
            return def;
        }

        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            // Ignore
        }

        logger.warn(
                "Unable to parse the long integer system property '{}':{} - using the default value: {}",
                key, value, def
        );

        return def;
    }

    private SystemPropertyUtil() {
        // Unused
    }
}
