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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.regex.Pattern;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import static org.awaitility.Awaitility.await;
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
    public void testExitCommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("exit\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() 
                -> os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> exit\r\n"));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testIAECommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("iae\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() 
                -> os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> iae\r\nError "
                        + "performing method invocation\r\njava.lang.IllegalArgumentException: iae\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedCommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("xxx\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> xxx\r\nUnknown command. Enter 'help' for a list of supported commands\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedSubCommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test nonexistent\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> test nonexistent\r\nUnknown sub command 'nonexistent'. Type 'test help' for more information"
                + "\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testSubcommand() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands"
                + "\n\rapp> test\r\nSupported subcommand for test\n\rexecute\t\ttest execute\n\rrun\t\ttest run"
                + "\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHelp() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("help\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands"
                + "\n\rapp> help\r\nSupported Commands\n\rdummy\t\tdummy description\n\rexit\t\tExit shell\n\rhealth"
                + "\t\tHealth of services\n\rhelp\t\tShow list of help commands\n\riae\t\tthrows IAE\n\rtest"
                + "\t\ttest description\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHealthCommandNoArg() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("health show\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("app> health show\r\nSupported health indicators "
                + "below:\n\r\tdiskspace\n\r\theapmemory\n\rUsage: health show <health indicator>\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHealthCommandUnsupportedHealthIndicator() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("health show unknown\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("app> health show unknown\r\nUnsupported health "
                + "indicator unknown\n\rSupported health indicators below:\n\r\tdiskspace\n\r\theapmemory\n\rUsage: "
                + "health show <health indicator>\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHealthCommandValidHealthIndicator() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("health show diskspace\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Pattern pattern = Pattern.compile(".*app> health show diskspace\r\n\\{\"status\":\"UP\",\"diskspace\":\\{.*",
                Pattern.DOTALL);
        await().atMost(2, SECONDS).until(() -> pattern.matcher(os.toString()).matches());
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHealthCommandHeapMemoryHealthIndicator() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("health show heapmemory\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Pattern pattern = Pattern.compile(".*app> health show heapmemory\r\n\\{\"heapmemory\":\\{.*",
                Pattern.DOTALL);
        await().atMost(2, SECONDS).until(() -> pattern.matcher(os.toString()).matches());
        channel.disconnect();
        session.disconnect();
    }
}
