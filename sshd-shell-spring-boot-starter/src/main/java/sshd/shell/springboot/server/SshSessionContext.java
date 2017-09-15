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
package sshd.shell.springboot.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

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

    static final String LINE_READER = "__lineReader";
    static final String TEXT_STYLE = "__textStyle";
    static final String TERMINAL = "__terminal";
    static final String USER = "__user";

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

    /**
     * Read input from line with mask. Use null if input is to be echoed. Use 0 if nothing is to be echoed and other
     * characters that get echoed with input
     *
     * @param text Text to show
     * @param mask mask
     * @return input from user
     * @throws IOException if any
     */
    public static String readInput(String text, Character mask) throws IOException {
        LineReader reader = get(LINE_READER);
        AttributedStyle textStyle = get(TEXT_STYLE);
        Terminal terminal = get(TERMINAL);
        return reader.readLine(new AttributedStringBuilder().style(textStyle).append(text).append(' ')
                .style(AttributedStyle.DEFAULT).toAnsi(terminal), mask);
    }

    /**
     * Read input from line with input echoed.
     *
     * @param text Text to show
     * @return input from user
     * @throws IOException if any
     */
    public static String readInput(String text) throws IOException {
        return readInput(text, null);
    }

    /**
     * Write output.
     *
     * @param text Text output
     */
    public static void writeOutput(String text) {
        Terminal terminal = get(TERMINAL);
        AttributedStyle textStyle = get(TEXT_STYLE);
        terminal.writer().println(new AttributedStringBuilder().style(textStyle).append(text)
                .style(AttributedStyle.DEFAULT).toAnsi(terminal));
        terminal.flush();
    }
}
