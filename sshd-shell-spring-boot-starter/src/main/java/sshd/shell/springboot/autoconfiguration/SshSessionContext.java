/*
 * Copyright 2017 anand.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author anand
 */
public enum SshSessionContext {

    ;

    private static final ThreadLocal<Map<String, Object>> THREAD_CONTEXT = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    public static void put(String key, Object value) {
        THREAD_CONTEXT.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <E> E get(String key) {
        return (E) THREAD_CONTEXT.get().get(key);
    }

    @SuppressWarnings("unchecked")
    public static <E> E remove(String key) {
        return (E) THREAD_CONTEXT.get().remove(key);
    }

    public static boolean containsKey(String key) {
        return THREAD_CONTEXT.get().containsKey(key);
    }

    public static boolean isEmpty() {
        return THREAD_CONTEXT.get().isEmpty();
    }

    public static void clear() {
        THREAD_CONTEXT.remove();
    }
}
