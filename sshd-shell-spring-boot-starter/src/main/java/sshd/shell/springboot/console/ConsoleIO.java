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
package sshd.shell.springboot.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.Objects;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
public enum ConsoleIO {
    ;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final ObjectWriter WRITER = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
    static final String LINE_READER = "__lineReader";
    static final String TEXT_STYLE = "__textStyle";
    static final String TERMINAL = "__terminal";
    static final String HIGHLIGHT_COLOR = "__highlightColor";

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
        LineReader reader = SshSessionContext.<LineReader>get(LINE_READER);
        AttributedStyle textStyle = SshSessionContext.<AttributedStyle>get(TEXT_STYLE);
        Terminal terminal = SshSessionContext.<Terminal>get(TERMINAL);
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
     * @param output Text output
     */
    public static void writeOutput(String output) {
        writeOutput(output, null);
    }

    /**
     * Write highlighted output.
     *
     * @param output Text output
     * @param textToHighlight text to highlight
     */
    public static void writeOutput(String output, String textToHighlight) {
        Terminal terminal = SshSessionContext.<Terminal>get(TERMINAL);
        AttributedStyle textStyle = SshSessionContext.<AttributedStyle>get(TEXT_STYLE);
        AttributedStringBuilder builder = new AttributedStringBuilder().style(textStyle);
        if (!Objects.isNull(textToHighlight)) {
            String[] split = output.split(textToHighlight);
            for (int i = 0; i < split.length - 1; i++) {
                builder.append(split[i]).style(SshSessionContext.<AttributedStyle>get(HIGHLIGHT_COLOR))
                        .append(textToHighlight).style(textStyle);
            }
            builder.append(split[split.length - 1]);
        } else {
            builder.append(output);
        }
        terminal.writer().println(builder.style(AttributedStyle.DEFAULT).toAnsi(terminal));
        terminal.flush();
    }

    public static void writeJsonOutput(Object object) {
        writeJsonOutput(object, null);
    }

    public static void writeJsonOutput(Object object, String textToHighlight) {
        writeOutput(asJson(object), textToHighlight);
    }

    public static String asJson(Object object) {
        try {
            return WRITER.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            log.error("Error processing json output", ex);
            return "Error processing json output: " + ex.getMessage();
        }
    }

    public static <E> E stringToObject(String json, Class<E> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(json, clazz);
    }
}
