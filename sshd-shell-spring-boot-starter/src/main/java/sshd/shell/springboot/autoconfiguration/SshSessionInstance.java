/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
class SshSessionInstance implements Command, ChannelSessionAware, Runnable {

    private static final String SUPPORTED_COMMANDS_MESSAGE = "Enter '" + Constants.HELP
            + "' for a list of supported commands";
    private static final String UNSUPPORTED_COMMANDS_MESSAGE = "Unknown command. " + SUPPORTED_COMMANDS_MESSAGE;
    private final SshdShellProperties.Shell properties;
    private final Map<String, Map<String, CommandExecutableDetails>> commandMap;
    private final Environment environment;
    private final Banner shellBanner;
    private InputStream is;
    private OutputStream os;
    private ExitCallback callback;
    private Thread sshThread;
    private PrintWriter writer;
    private ChannelSession session;

    SshSessionInstance(SshdShellProperties properties, Map<String, Map<String, CommandExecutableDetails>> commandMap,
            Environment environment, Banner shellBanner) {
        this.properties = properties.getShell();
        this.commandMap = commandMap;
        this.environment = environment;
        this.shellBanner = shellBanner;
    }

    @Override
    public void start(org.apache.sshd.server.Environment env) throws IOException {
        sshThread = new Thread(this, "sshd-cli");
        sshThread.start();
    }

    @Override
    public void run() {
        shellBanner.printBanner(environment, this.getClass(), new PrintStream(os));
        try (Terminal terminal = TerminalBuilder.builder().system(false).streams(is, os).build()) {
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            writer = terminal.writer();
            createDefaultSessionContext(reader);
            SshSessionContext.writeOutput(SUPPORTED_COMMANDS_MESSAGE);
            String line;
            String prompt = AnsiOutput.encode(properties.getPrompt().getColor()) + properties.getPrompt().getTitle()
                    + "> " + AnsiOutput.encode(AnsiColor.DEFAULT);
            while ((line = reader.readLine(prompt)) != null) {
                try {
                    handleUserInput(line.trim());
                } catch (InterruptedException ex) {
                    log.info(ex.getMessage());
                    SshSessionContext.writeOutput(ex.getMessage());
                    cleanup();
                    break;
                }
            }
        } catch (IOException ex) {
            log.error("Error building terminal instance", ex);
        } finally {
            cleanup();
        }
    }

    private void createDefaultSessionContext(LineReader reader) {
        SshSessionContext.put(SshSessionContext.LINE_READER, reader);
        SshSessionContext.put(SshSessionContext.TEXT_COLOR, properties.getText().getColor());
        SshSessionContext.put(SshSessionContext.WRITER, writer);
        SshSessionContext.put(Constants.USER_ROLES, session.getSession().getIoSession()
                .getAttribute(Constants.USER_ROLES));
    }

    private void handleUserInput(String userInput) throws InterruptedException {
        String[] part = userInput.split(" ", 3);
        String command = part[0];
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(command);
        if (Objects.isNull(commandExecutables)) {
            SshSessionContext.writeOutput(UNSUPPORTED_COMMANDS_MESSAGE);
            return;
        }
        CommandExecutableDetails ced = commandExecutables.get(Constants.EXECUTE);
        Collection<String> userRoles = SshSessionContext.<Collection<String>>get(Constants.USER_ROLES);
        if (!ced.matchesRole(userRoles)) {
            SshSessionContext.writeOutput("Permission denied");
            return;
        }
        if (part.length < 2) {
            if (Objects.isNull(ced.getCommandExecutor())) {
                StringBuilder sb = new StringBuilder("Supported subcommand for ").append(command);
                commandExecutables.entrySet().stream()
                        .filter(e -> !e.getKey().equals(Constants.EXECUTE) && e.getValue().matchesRole(userRoles))
                        .forEach(e -> sb.append("\n\r").append(e.getKey()).append("\t\t")
                        .append(e.getValue().getDescription()));
                SshSessionContext.writeOutput(sb.toString());
            } else {
                SshSessionContext.writeOutput(ced.getCommandExecutor().get(null));
            }
        } else if (commandExecutables.containsKey(part[1])) {
            String subCommand = part[1];
            ced = commandExecutables.get(subCommand);
            if (!ced.matchesRole(userRoles)) {
                SshSessionContext.writeOutput("Permission denied");
            } else {
                SshSessionContext.writeOutput(commandExecutables.get(subCommand).getCommandExecutor()
                        .get(part.length == 2 ? null : part[2]));
            }
        } else {
            SshSessionContext.writeOutput("Unknown sub command '" + part[1] + "'. Type '" + part[0]
                    + " help' for more information");
        }
    }

    private void cleanup() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            log.error("Interrupted exception", ex);
        } finally {
            SshSessionContext.clear();
            callback.onExit(0);
        }
    }

    @Override
    public void destroy() throws Exception {
        sshThread.interrupt();
    }

    @Override
    public void setErrorStream(OutputStream errOS) {
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        callback = ec;
    }

    @Override
    public void setInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void setChannelSession(ChannelSession session) {
        this.session = session;
    }
}
