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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import sshd.shell.springboot.autoconfiguration.ColorType;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor
@lombok.extern.slf4j.Slf4j
class TerminalProcessor {

    private static final String SUPPORTED_COMMANDS_MESSAGE = "Enter '" + Constants.HELP
            + "' for a list of supported commands";
    private static final String UNSUPPORTED_COMMANDS_MESSAGE = "Unknown command. " + SUPPORTED_COMMANDS_MESSAGE;

    private final InputStream is;
    private final OutputStream os;
    private final SshdShellProperties.Shell properties;
    private final Map<String, Map<String, CommandExecutableDetails>> commandMap;
    private final Completer completer;
    private final String terminalType;

    void processInputs() {
        try (Terminal terminal = TerminalBuilder.builder().system(false).type(terminalType).streams(is, os).build()) {
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
            createDefaultSessionContext(reader, terminal);
            SshSessionContext.writeOutput(SUPPORTED_COMMANDS_MESSAGE);
            processUserInput(reader);
        } catch (IOException ex) {
            log.error("Error building terminal instance", ex);
        }
    }

    private void createDefaultSessionContext(LineReader reader, Terminal terminal) {
        SshSessionContext.put(SshSessionContext.LINE_READER, reader);
        SshSessionContext.put(SshSessionContext.TEXT_STYLE, getStyle(properties.getText().getColor()));
        SshSessionContext.put(SshSessionContext.TERMINAL, terminal);
    }

    private AttributedStyle getStyle(ColorType color) {
        // This check is done to allow for default contrasting color texts to be shown on black or white screens
        return color == ColorType.BLACK || color == ColorType.WHITE ? AttributedStyle.DEFAULT
                : AttributedStyle.DEFAULT.foreground(color.value);
    }

    private void processUserInput(LineReader reader) throws UserInterruptException, EndOfFileException {
        String prompt = new AttributedStringBuilder().style(getStyle(properties.getPrompt().getColor()))
                .append(properties.getPrompt().getTitle()).append("> ").style(AttributedStyle.DEFAULT).toAnsi();
        String line;
        while ((line = reader.readLine(prompt)) != null) {
            try {
                handleUserInput(line.trim());
            } catch (InterruptedException | UserInterruptException ex) {
                log.info(ex.getMessage());
                SshSessionContext.writeOutput(ex.getMessage());
                break;
            } catch (ShellException ex) {
                SshSessionContext.writeOutput(ex.getMessage());
            }
        }
    }

    private void handleUserInput(String userInput) throws InterruptedException, ShellException {
        if (!userInput.isEmpty()) {
            String[] part = userInput.split(" ", 3); // Three parts: command, subcommand, arg
            Collection<String> userRoles = getUserRoles(part[0]);
            if (part.length < 2) {
                handleSingleTokenUserInput(part[0], userRoles);
            } else {
                handleUserInputWithMoreTokens(part, userRoles);
            }
        }
    }

    private Collection<String> getUserRoles(String command) throws ShellException {
        Collection<String> userRoles = SshSessionContext.<Collection<String>>get(Constants.USER_ROLES);
        Map<String, CommandExecutableDetails> commandExecutables = getCommandExecutables(command);
        CommandExecutableDetails ced = commandExecutables.get(Constants.EXECUTE);
        validateExecutableWithUserRole(ced, userRoles);
        return userRoles;
    }

    private Map<String, CommandExecutableDetails> getCommandExecutables(String command) throws ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(command);
        if (Objects.isNull(commandExecutables)) {
            throw new ShellException(UNSUPPORTED_COMMANDS_MESSAGE);
        }
        return commandExecutables;
    }

    private void validateExecutableWithUserRole(CommandExecutableDetails ced, Collection<String> userRoles) throws
            ShellException {
        if (!ced.matchesRole(userRoles)) {
            throw new ShellException("Permission denied");
        }
    }

    private void handleSingleTokenUserInput(String command, Collection<String> userRoles) throws InterruptedException {
        CommandExecutableDetails ced = commandMap.get(command).get(Constants.EXECUTE);
        SshSessionContext.writeOutput(Objects.isNull(ced.getCommandExecutor())
                ? unknownSubcommandMessage(command, userRoles) : ced.executeWithArg(null));
    }

    private String unknownSubcommandMessage(String command, Collection<String> userRoles) {
        StringBuilder sb = new StringBuilder("Supported subcommand for ").append(command);
        commandMap.get(command).entrySet().stream()
                .filter(e -> !e.getKey().equals(Constants.EXECUTE) && e.getValue().matchesRole(userRoles))
                .forEach(e -> sb.append("\n\r").append(e.getKey()).append("\t\t")
                .append(e.getValue().getDescription()));
        return sb.toString();
    }

    private void handleUserInputWithMoreTokens(String[] part, Collection<String> userRoles) throws
            InterruptedException, ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(part[0]);
        if (!commandExecutables.containsKey(part[1])) {
            throw new ShellException("Unknown subcommand '" + part[1] + "'. Type '" + part[0]
                    + "' for supported subcommands");
        }
        CommandExecutableDetails ced = commandMap.get(part[0]).get(part[1]);
        validateExecutableWithUserRole(ced, userRoles);
        SshSessionContext.writeOutput(ced.executeWithArg(part.length == 2 ? null : part[2]));
    }

    private static class ShellException extends Exception {

        private static final long serialVersionUID = 7114130906989289480L;

        public ShellException(String message) {
            super(message);
        }
    }
}
