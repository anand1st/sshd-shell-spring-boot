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
            verifyResponse(is, "Error performing method invocation\r\r\njava.lang.IllegalArgumentException: iae");
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
            verifyResponse(is, "Unknown sub command 'nonexistent'. Type 'test help' for more information");
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
            verifyResponse(is, "Supported Commands\r\n\rdummy\t\tdummy description\r\n\rexit\t\tExit shell"
                + "\r\n\rhealth\t\tHealth of services\r\n\rhelp\t\tShow list of help commands\r\n\riae\t\tthrows IAE"
                + "\r\n\rtest\t\ttest description");
        });
    }

    @Test
    public void testHealthCommandNoArg() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "health show");
            verifyResponse(is, "Supported health indicators below:\r\n\r\tdiskspace\r\n\r\theapmemory\r\n\r"
                + "Usage: health show <health indicator>");
        });
    }

    @Test
    public void testHealthCommandUnsupportedHealthIndicator() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "health show unknown");
            verifyResponse(is, "Unsupported health indicator unknown\r\n\rSupported health indicators "
                + "below:\r\n\r\tdiskspace\r\n\r\theapmemory\r\n\rUsage: health show <health indicator>");
        });
    }

    @Test
    public void testHealthCommandValidHealthIndicator() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "health show diskspace");
            verifyResponse(is, "{\"status\":\"UP\",\"diskspace\":{");
        });
    }

    @Test
    public void testHealthCommandHeapMemoryHealthIndicator() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "health show heapmemory");
            verifyResponse(is, "{\"heapmemory\":{");
        });
    }
    
    @Test
    public void testInteractive() throws JSchException, IOException {
        sshCall((is, os) -> {
            write(os, "test interactive", "anand");
            verifyResponse(is, "Name: anandHi anand");
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
