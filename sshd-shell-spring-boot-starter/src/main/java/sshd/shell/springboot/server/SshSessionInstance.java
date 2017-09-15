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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import org.apache.sshd.server.ChannelSessionAware;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;
import sshd.shell.springboot.autoconfiguration.TerminalProcessor;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
class SshSessionInstance implements Command, ChannelSessionAware, Runnable {

    private final SshdShellProperties.Shell properties;
    private final Map<String, Map<String, CommandExecutableDetails>> commandMap;
    private final Environment environment;
    private final Banner shellBanner;
    private InputStream is;
    private OutputStream os;
    private ExitCallback callback;
    private Thread sshThread;
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
        sshThread = new Thread(this, "ssh-cli " + session.getSession().getIoSession().getAttribute(Constants.USER));
        sshThread.start();
    }

    @Override
    public void run() {
        shellBanner.printBanner(environment, this.getClass(), new PrintStream(os));
        SshSessionContext.put(Constants.USER_ROLES, session.getSession().getIoSession()
                .getAttribute(Constants.USER_ROLES));
        try {
            new TerminalProcessor(is, os, properties, commandMap).processInputs();
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
