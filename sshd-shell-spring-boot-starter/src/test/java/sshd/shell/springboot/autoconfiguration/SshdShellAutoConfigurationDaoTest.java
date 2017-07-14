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
package sshd.shell.springboot.autoconfiguration;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import static java.util.concurrent.TimeUnit.SECONDS;
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
@SpringBootTest(classes = ConfigTest.class, properties = {"sshd.shell.auth.authType=DAO", "sshd.shell.username=bob",
    "sshd.shell.password=bob"})
public class SshdShellAutoConfigurationDaoTest {

    @Autowired
    private SshdShellProperties properties;
    
    @Test
    public void testDaoAuthWithoutRightPermission() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test run bob\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> test run bob\r\nPermission denied\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testDaoAuthWithoutRightPermission2() throws JSchException {
        JSch jsch = new JSch();
        // See ConfigTest.java for why username is alice
        Session session = jsch.getSession("alice", "localhost", properties.getShell().getPort());
        session.setPassword("alice");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test run\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> test run\r\nPermission denied\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testDaoAuthWithRightPermission() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test execute bob\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        await().atMost(2, SECONDS).until(() -> os.toString().contains("Enter 'help' for a list of supported commands\n"
                + "\rapp> test execute bob\r\ntest execute successful\n\rapp> "));
        channel.disconnect();
        session.disconnect();
    }
    
    @Test(expected = JSchException.class)
    public void testDaoFailedAuth() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getShell().getUsername(), "localhost",
                properties.getShell().getPort());
        session.setPassword("wrong");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
    }
}
