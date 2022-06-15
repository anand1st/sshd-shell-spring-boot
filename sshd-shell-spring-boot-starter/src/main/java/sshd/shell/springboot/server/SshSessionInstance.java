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
package sshd.shell.springboot.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.console.TerminalProcessor;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
class SshSessionInstance implements Command, Factory<Command>, Runnable {

    private final TerminalProcessor terminalProcessor;
    private final Optional<String> rootedFileSystemBaseDir;
    private final BiConsumer<Class<?>, PrintStream> shellBannerPrinter;
    private InputStream is;
    private OutputStream os;
    private ExitCallback exitCallback;
    private Thread sshThread;
    private ChannelSession channel;
    private String terminalType;

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        terminalType = env.getEnv().get(Environment.ENV_TERM);
        this.channel = channel;
        sshThread = new Thread(this, "ssh-cli " + channel.getSession().getIoSession().getAttribute(Constants.USER));
        sshThread.start();
    }

    @Override
    public void run() {
        shellBannerPrinter.accept(this.getClass(), new PrintStream(os));
        populateSessionContext();
        try {
            terminalProcessor.processInputs(is, os, terminalType, exitCode -> exitCallback.onExit(exitCode));
        } finally {
            SshSessionContext.clear();
        }
    }

    private void populateSessionContext() {
        SshSessionContext.put(Constants.USER, channel.getSession().getIoSession().getAttribute(Constants.USER));
        SshSessionContext.put(Constants.USER_ROLES, channel.getSession().getIoSession()
                .getAttribute(Constants.USER_ROLES));
        SshSessionContext.setUserDir(() -> getRootedUserDir());
    }

    private File getRootedUserDir() {
        return new File(rootedFileSystemBaseDir.orElseThrow(() -> new IllegalStateException("SCP/SFTP is not enabled")),
                SshSessionContext.<String>get(Constants.USER));
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        channel.close();
        sshThread.interrupt();
    }

    @Override
    public void setErrorStream(OutputStream errOS) {
        // Ignore
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        exitCallback = ec;
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
    public Command create() {
        return this;
    }
}
