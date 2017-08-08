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

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author anand
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ConfigTest.class)
public class SshdShellAutoConfigurationTest {

    @Autowired
    private SshdShellProperties properties;

    @Test
    public void testExitCommand() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("exit\r".getBytes(StandardCharsets.UTF_8));
//        ConfigTest.checkResponse(pis, "Exiting shell");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testIAECommand() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("iae\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Error performing method invocation\r\r\njava.lang.IllegalArgumentException: "
                + "iae");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testUnsupportedCommand() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("xxx\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Unknown command. Enter 'help' for a list of supported commands");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testUnsupportedSubCommand() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("test nonexistent\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Unknown sub command 'nonexistent'. Type 'test help' for more information");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testSubcommand() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("test\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Supported subcommand for test\r\n\rexecute\t\ttest execute\r\n\rinteractive\t\ttest interactive\r\n\rrun\t\ttest "
                + "run");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testHelp() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("help\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Supported Commands\r\n\rdummy\t\tdummy description\r\n\rexit\t\tExit shell\r\n\r"
                + "health\t\tHealth of services\r\n\rhelp\t\tShow list of help commands\r\n\riae\t\tthrows IAE\r\n\r"
                + "test\t\ttest description");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testHealthCommandNoArg() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("health show\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Supported health indicators below:\r\n\r\tdiskspace\r\n\r\theapmemory\r\n\r"
                + "Usage: health show <health indicator>");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testHealthCommandUnsupportedHealthIndicator() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("health show unknown\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Unsupported health indicator unknown\r\n\rSupported health indicators below:"
                + "\r\n\r\tdiskspace\r\n\r\theapmemory\r\n\rUsage: health show <health indicator>");
        channel.disconnect();
        session.disconnect();
    }

    @Ignore
    @Test
    public void testHealthCommandValidHealthIndicator() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("health show diskspace\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "{\"status\":\"UP\",\"diskspace\":{");
        channel.disconnect();
        session.disconnect();
    }

    @Test
    public void testHealthCommandHeapMemoryHealthIndicator() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("health show heapmemory\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "{\"heapmemory\":{");
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testInteractive() throws JSchException, IOException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();
        channel.setInputStream(new PipedInputStream(pos));
        channel.setOutputStream(new PipedOutputStream(pis));
        channel.connect();
        pos.write("test interactive\r".getBytes(StandardCharsets.UTF_8));
        pos.write("anand\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Name: anandHi anand");
        channel.disconnect();
        session.disconnect();
    }
}
