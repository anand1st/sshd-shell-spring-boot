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
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    private Map<String, Map<String, CommandSupplier>> sshdCliCommands;
    @Autowired
    private SshdShellProperties properties;

    @Test
    public void testExpectedDataFromMethodCallsAndHelp() throws Exception {
        assertEquals(5, sshdCliCommands.size());
        assertEquals("Supported Commands\n\rdummy\t\tdummy description\n\rexit\t\tExit shell\n\riae\t\tthrows IAE\n\r"
                + "test\t\ttest description", sshdCliCommands.get("help").get(SshdShellAutoConfiguration.EXECUTE)
                        .get(null));
        assertEquals("test description\n\r\trun\t\ttest run\n\r\texecute\t\ttest execute",
                sshdCliCommands.get("test").get(SshdShellAutoConfiguration.HELP).get(null));
        assertEquals("test run successful", sshdCliCommands.get("test").get("run").get("successful"));
        assertEquals("test execute successful", sshdCliCommands.get("test").get("execute").get(null));
        assertEquals("dummy run successful", sshdCliCommands.get("dummy").get("run").get(null));
        assertEquals("dummy description\n\r\trun\t\tdummy run",
                sshdCliCommands.get("dummy").get(SshdShellAutoConfiguration.HELP).get(null));
        assertEquals("Exit shell", sshdCliCommands.get("exit").get(SshdShellAutoConfiguration.HELP).get(null));
    }
    
    @Test
    public void testExitCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
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
        Thread.sleep(1000);
        System.out.println(os.toString());
        assertTrue(os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> exit\r\n"));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testIAECommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
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
        Thread.sleep(1000);
        assertTrue(os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> iae\r\n"));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
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
        Thread.sleep(1000);
        assertTrue(os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> xxx\r\nUnknown "
                + "command. Enter 'help' for a list of supported commands\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedSubCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
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
        Thread.sleep(1000);
        assertTrue(os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> test nonexistent\r\n"
                + "Unknown sub command 'nonexistent'. Type 'test help' for more information\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHelpCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
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
        Thread.sleep(1000);
        assertTrue(os.toString().contains("Enter 'help' for a list of supported commands\n\rapp> test\r\ntest "
                + "description\n\r\trun\t\ttest run\n\r\texecute\t\ttest execute\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
}
