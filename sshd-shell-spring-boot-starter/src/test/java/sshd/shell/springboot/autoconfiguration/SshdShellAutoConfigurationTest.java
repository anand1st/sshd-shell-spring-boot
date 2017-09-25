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

import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author anand
 */
public class SshdShellAutoConfigurationTest extends AbstractSshSupport {

    //FIXME Figure out why following test case fails on command line but passes on IDE
    @Ignore
    @Test
    public void testExitCommand() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "exit");
            verifyResponse(is, "Exiting shell");
        });
    }

    @Test
    public void testIAECommand() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "iae");
            verifyResponse(is, "Error performing method invocation\r\r\nPlease check server logs for more information");
        });
    }

    @Test
    public void testUnsupportedCommand() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "xxx");
            verifyResponse(is, "Unknown command. Enter 'help' for a list of supported commands");
        });
    }

    @Test
    public void testUnsupportedSubCommand() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "test nonexistent");
            verifyResponse(is, "Unknown subcommand 'nonexistent'. Type 'test' for supported subcommands");
        });
    }

    @Test
    public void testSubcommand() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "test");
            verifyResponse(is, "Supported subcommand for test\r\n\rexecute\t\ttest execute\r\n\rinteractive"
                    + "\t\ttest interactive\r\n\rrun\t\ttest run");
        });
    }

    @Test
    public void testHelp() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "help");
            StringBuilder format = new StringBuilder("Supported Commands");
            for (int i = 0; i < 6; i++) {
                format.append("\r\n%-16s%s");
            }
            verifyResponse(is, String.format(Locale.ENGLISH, format.toString(), "dummy", "dummy description",
                    "endpoint", "Invoke actuator endpoints", "exit", "Exit shell", "help", "Show list of help commands",
                    "iae", "throws IAE", "test", "test description"));
        });
    }

    @Test
    public void testInteractive() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "test interactive", "anand");
            verifyResponse(is, "Name: anandHi anand");
        });
    }

    @Test
    public void testEndpointList() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "endpoint list");
            verifyResponse(is, getEndpointList());
        });
    }

    private String getEndpointList() {
        String[] endpoints = {"autoconfig", "beans", "configprops", "dump", "env", "health", "info", "loggers",
            "metrics", "trace", "shutdown"};
        String[] enabled = {"true", "true", "true", "true", "true", "true", "true", "true", "true", "true", "false"};
        StringBuilder sb = new StringBuilder(String.format(Locale.ENGLISH, "%-16s%s\r\n", "Endpoints",
                "Is Enabled?")).append("---------       -----------");
        assertEquals(endpoints.length, enabled.length);
        for (int i = 0; i < endpoints.length; i++) {
            sb.append(String.format(Locale.ENGLISH, "\r\n%-16s%s", endpoints[i], enabled[i]));
        }
        return sb.toString();
    }

    @Test
    public void testEndpointNullArg() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "endpoint invoke");
            String response = "Null or unknown endpoint\r\n" + getEndpointList() 
                    + "\r\nUsage: endpoint invoke <endpoint>";
            verifyResponse(is, response);
        });
    }
    
    @Test
    public void testEndpointInvalid() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "endpoint invoke invalid");
            String response = "Null or unknown endpoint\r\n" + getEndpointList() 
                    + "\r\nUsage: endpoint invoke <endpoint>";
            verifyResponse(is, response);
        });
    }
    
    @Test
    public void testEndpointDisabled() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "endpoint invoke shutdown");
            verifyResponse(is, "Endpoint shutdown is not enabled");
        });
    }
    
    @Test
    public void testEndpointInvokeSuccess() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "endpoint invoke info");
            verifyResponse(is, "{ }");
        });
    }

    // FIXME Figure out why following test case fails with command line call but passes with IDE
    @Ignore
    @Test
    public void testEmptyUserInput() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "");
            verifyResponse(is, "app> app>");
        });
    }
}
