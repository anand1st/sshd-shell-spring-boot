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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Locale;
import java.util.Properties;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author anand
 */
@SpringBootTest(classes = ConfigTest.class, properties = {
        "sshd.shell.auth.authType=AUTH_PROVIDER",
        "sshd.shell.username=bob",
        "sshd.shell.password=bob",
        "spring.flyway.baseline-on-migrate=true",
        "spring.main.allow-circular-references=true"
})
@DirtiesContext
public class SshdShellAutoConfigurationAuthProviderTest extends AbstractSshSupport {

    @Test
    public void testDaoAuthWithoutRightPermission() {
        sshCallShell((is, os) -> {
            write(os, "test run bob");
            verifyResponseContains(is, "Permission denied");
        });
    }

    @Test
    public void testDaoAuthWithoutRightPermission2() {
        // See ConfigTest.java for why username is alice
        sshCall("alice", "alice", (is, os) -> {
            write(os, "test run");
            verifyResponseContains(is, "Permission denied");
        }, "exec");
    }

    @Test
    public void testDaoAuthWithRightPermission() {
        sshCallShell((is, os) -> {
            write(os, "test execute bob");
            verifyResponseContains(is, "test execute successful");
        });
    }

    @Test(expected = JSchException.class)
    public void testDaoFailedAuth() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(props.getShell().getUsername(), props.getShell().getHost(),
                props.getShell().getPort());
        session.setPassword("wrong");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
    }

    @Test
    public void testHelp() {
        // See ConfigTest.java for why username is alice
        sshCall("alice", "alice", (is, os) -> {
            String format = "\r" + props.getShell().getText().getUsageInfoFormat();
            write(os, "help");
            StringBuilder sb = new StringBuilder("Supported Commands");
            for (int i = 0; i < 4; i++) {
                sb.append(format);
            }
            sb.append("\r\nSupported post processors for output")
                    .append(String.format(Locale.ENGLISH, format, "h <arg>",
                            "Highlights <arg> in response output of command execution"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | h exit"))
                    .append(String.format(Locale.ENGLISH, format, "m <emailId>",
                            "Send response output of command execution to <emailId>"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | m bob@hope.com"));
            verifyResponseContains(is, String.format(Locale.ENGLISH, sb.toString(),
                    "dummy", "dummy description",
                    "exception", "throws Exceptions",
                    "exit", "Exit shell",
                    "help", "Show list of help commands"));
        }, "shell");
    }
}
