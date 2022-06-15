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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sshd.shell.springboot.ShellException;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties.Shell;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
public class TerminalProcessor {

    private static final String SUPPORTED_COMMANDS_MESSAGE = "Enter '" + Constants.HELP
            + "' for a list of supported commands";
    static final String UNSUPPORTED_COMMANDS_MESSAGE = "Unknown command. " + SUPPORTED_COMMANDS_MESSAGE;

    private final Shell properties;
    private final Completer completer;
    private final List<BaseUserInputProcessor> userInputProcessors;
    private final String prompt;

    TerminalProcessor(Shell properties, Completer completer, List<BaseUserInputProcessor> userInputProcessors) {
        this.properties = properties;
        this.completer = completer;
        this.userInputProcessors = userInputProcessors;
        prompt = new AttributedStringBuilder()
                .style(getStyle(properties.getPrompt().getColor()))
                .append(properties.getPrompt().getTitle())
                .append("> ")
                .style(AttributedStyle.DEFAULT)
                .toAnsi();
    }

    public void processInputs(InputStream is, OutputStream os, String terminalType, IntConsumer exitCallback) {
        try (Terminal terminal = newTerminalInstance(terminalType, is, os)) {
            LineReader reader = newLineReaderInstance(terminal);
            createDefaultSessionContext(reader, terminal);
            ConsoleIO.writeOutput(SUPPORTED_COMMANDS_MESSAGE);
            processInputs(reader, exitCallback);
        } catch (IOException ex) {
            log.error("Error building terminal instance", ex);
        }
    }

    private Terminal newTerminalInstance(String terminalType, InputStream is, OutputStream os) throws IOException {
        return TerminalBuilder.builder()
                .system(false)
                .type(terminalType)
                .streams(is, os)
                .build();
    }

    private LineReader newLineReaderInstance(final Terminal terminal) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
    }

    private void createDefaultSessionContext(LineReader reader, Terminal terminal) {
        SshSessionContext.put(ConsoleIO.LINE_READER, reader);
        SshSessionContext.put(ConsoleIO.TEXT_STYLE, getStyle(properties.getText().getColor()));
        SshSessionContext.put(ConsoleIO.HIGHLIGHT_COLOR,
                AttributedStyle.DEFAULT.background(properties.getText().getHighlightColor().value));
        SshSessionContext.put(ConsoleIO.TERMINAL, terminal);
    }

    private AttributedStyle getStyle(ColorType color) {
        // This check is done to allow for default contrasting color texts to be shown on black or white screens
        return color == ColorType.BLACK || color == ColorType.WHITE
                ? AttributedStyle.DEFAULT
                : AttributedStyle.DEFAULT.foreground(color.value);
    }

    private void processInputs(LineReader reader, IntConsumer exitCallback) {
        try {
            processUserInput(reader, exitCallback);
        } catch (UserInterruptException ex) {
            // Need not concern with this exception
            log.warn("[{}] Ctrl-C interrupt", SshSessionContext.<String>get(Constants.USER));
            exitCallback.accept(1);
        }
    }

    private void processUserInput(LineReader reader, IntConsumer exitCallback) {
        while (true) {
            try {
                handleUserInput(reader.readLine(prompt).trim());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupted();
                ConsoleIO.writeOutput(ex.getMessage());
                exitCallback.accept(0);
                break;
            } catch (ShellException | IllegalArgumentException ex) {
                ConsoleIO.writeOutput(ex.getMessage());
            }
        }
    }

    private void handleUserInput(String userInput) throws InterruptedException, ShellException {
        if (!userInput.isEmpty()) {
            log.info("[{}] Executed command: {}", SshSessionContext.<String>get(Constants.USER), userInput);
            for (BaseUserInputProcessor userInputProcessor : userInputProcessors) {
                Matcher matcher = userInputProcessor.getPattern().matcher(userInput);
                if (matcher.matches()) {
                    userInputProcessor.processUserInput(userInput);
                    return;
                }
            }
            throw new ShellException("Unsupported command post processor! Should not happen");
        }
    }
}
