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
import java.util.Map;
import java.util.Objects;
import jline.console.ConsoleReader;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
class SshSessionInstance implements Command, Runnable {

    private static final String SUPPORTED_COMMANDS_MESSAGE = "Enter '" + SshdShellAutoConfiguration.HELP
            + "' for a list of supported commands";
    private static final String UNSUPPORTED_COMMANDS_MESSAGE = "Unknown command. " + SUPPORTED_COMMANDS_MESSAGE;
    private final SshdShellProperties properties;
    private final Map<String, Map<String, CommandSupplier>> commandMap;
    private final Environment environment;
    private final Banner shellBanner;
    private InputStream is;
    private OutputStream os;
    private ExitCallback callback;
    private Thread sshThread;
    private PrintWriter writer;

    SshSessionInstance(SshdShellProperties properties, Map<String, Map<String, CommandSupplier>> commandMap,
            Environment environment, Banner shellBanner) {
        this.properties = properties;
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
        try (ConsoleReader reader = new ConsoleReader(is, os)) {
            reader.setPrompt(AnsiOutput.encode(properties.getShell().getPrompt().getColor())
                    + properties.getShell().getPrompt().getTitle() + "> " + AnsiOutput.encode(AnsiColor.DEFAULT));
            writer = new PrintWriter(reader.getOutput());
            writeResponse(SUPPORTED_COMMANDS_MESSAGE);
            createDefaultSessionContext();
            String line;
            while ((line = reader.readLine()) != null) {
                handleUserInput(line.trim());
            }
        } catch (IOException ex) {
            log.error("Error with console reader", ex);
        } catch (InterruptedException ex) {
            log.info(ex.getMessage());
        } finally {
            SshSessionContext.clear();
            callback.onExit(0);
        }
    }
    
    private void createDefaultSessionContext() throws IOException {
        SshSessionContext.put(SshSessionContext.CONSOLE_READER, new ConsoleReader(is, os));
        SshSessionContext.put(SshSessionContext.TEXT_COLOR, properties.getShell().getText().getColor());
    }

    private void writeResponse(String response) {
        writer.println(AnsiOutput.encode(properties.getShell().getText().getColor()) + response);
        writer.write(ConsoleReader.RESET_LINE);
        writer.flush();
    }

    private void handleUserInput(String command) throws InterruptedException {
        String[] part = command.split(" ", 3);
        Map<String, CommandSupplier> supplier = commandMap.get(part[0]);
        if (Objects.isNull(supplier)) {
            writeResponse(UNSUPPORTED_COMMANDS_MESSAGE);
        } else if (part.length < 2) {
            writeResponse((supplier.containsKey(SshdShellAutoConfiguration.EXECUTE)
                    ? supplier.get(SshdShellAutoConfiguration.EXECUTE)
                    : supplier.get(SshdShellAutoConfiguration.HELP)).get(null));
        } else if (supplier.containsKey(part[1])) {
            writeResponse(supplier.get(part[1]).get(part.length == 2 ? null : part[2]));
        } else {
            writeResponse("Unknown sub command '" + part[1] + "'. Type '" + part[0] + " help' for more information");
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
}
