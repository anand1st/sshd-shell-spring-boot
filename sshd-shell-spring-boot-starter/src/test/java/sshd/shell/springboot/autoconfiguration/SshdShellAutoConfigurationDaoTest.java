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
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
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
    public void testDaoAuthWithoutRightPermission() throws JSchException, IOException {
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
        pos.write("test run bob\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Permission denied");
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testDaoAuthWithoutRightPermission2() throws JSchException, IOException {
        JSch jsch = new JSch();
        // See ConfigTest.java for why username is alice
        Session session = jsch.getSession("alice", "localhost", properties.getShell().getPort());
        session.setPassword("alice");
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
        pos.write("test run\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "Permission denied");
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testDaoAuthWithRightPermission() throws JSchException, IOException {
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
        pos.write("test execute bob\r".getBytes(StandardCharsets.UTF_8));
        ConfigTest.checkResponse(pis, "test execute successful");
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
