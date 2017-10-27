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

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.SocketUtils;

/**
 *
 * @author anand
 */
public class SshdShellAutoConfigurationTest extends AbstractSshSupport {

    @Autowired
    private JavaMailSender mailSender;

    //FIXME Figure out why following test case fails when run as part of test suite but passes when run as focused test
    @Ignore
    @Test
    public void testExitCommand() {
        sshCall((is, os) -> {
            write(os, "exit");
            verifyResponse(is, "Exiting shell");
        });
    }

    @Test
    public void testEmptyUserInput() {
        sshCall((is, os) -> {
            write(os, "");
            verifyResponse(is, "\r\r\n");
        });
    }

    @Test
    public void testIAECommand() {
        sshCall((is, os) -> {
            write(os, "iae");
            verifyResponse(is, "Error performing method invocation\r\r\nPlease check server logs for more information");
        });
    }

    @Test
    public void testUnsupportedCommand() {
        sshCall((is, os) -> {
            write(os, "xxx");
            verifyResponse(is, "Unknown command. Enter 'help' for a list of supported commands");
        });
    }

    @Test
    public void testUnsupportedSubCommand() {
        sshCall((is, os) -> {
            write(os, "test nonexistent");
            verifyResponse(is, "Unknown subcommand 'nonexistent'. Type 'test' for supported subcommands");
        });
    }

    @Test
    public void testSubcommand() {
        sshCall((is, os) -> {
            write(os, "test");
            verifyResponse(is, "Supported subcommand for test\r\n\rexecute\t\ttest execute\r\n\rinteractive"
                    + "\t\ttest interactive\r\n\rrun\t\ttest run");
        });
    }

    @Test
    public void testHelp() {
        sshCall((is, os) -> {
            String format = "\r\n%-35s%s"; // must be same with usageInfoFormat in SshdShellProperties.java
            write(os, "help");
            StringBuilder sb = new StringBuilder("Supported Commands");
            for (int i = 0; i < 19; i++) {
                sb.append(format);
            }
            sb.append("\r\nSupported post processors for output")
                    .append(String.format(Locale.ENGLISH, format, "h <arg>",
                            "Highlights <arg> in response output of command execution"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | h exit"))
                    .append(String.format(Locale.ENGLISH, format, "m <emailId>",
                            "Send response output of command execution to <emailId>"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | m bob@hope.com"));
            verifyResponse(is, String.format(Locale.ENGLISH, sb.toString(), "auditEvents", "Event auditing",
                    "autoConfigurationReport", "Autoconfiguration report", "beans", "List beans",
                    "configurationPropertiesReport", "Configuration properties report", "dummy", "dummy description",
                    "environment", "Environment details", "exit", "Exit shell", "health", "System health info", "help",
                    "Show list of help commands", "iae", "throws IAE", "info", "System status", "loggers",
                    "Logging configuration", "metrics", "Metrics operations", "requestMapping",
                    "Request mapping information", "shutdown", "Shutdown application", "status", "System status",
                    "test", "test description", "threadDump", "Print thread dump", "traces", "Trace information"));
        });
    }

    @Test
    public void testInteractive() {
        sshCall((is, os) -> {
            write(os, "test interactive", "anand");
            verifyResponse(is, "Name: anand{\r\n  \"obj\" : \"anand\"\r\n}\r\nHi anand");
        });
    }

    @Test
    public void testMailProcessor() {
        int smtpPort = SocketUtils.findAvailableTcpPort();
        ServerSetup setup = new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        GreenMail mailServer = new GreenMail(setup);
        mailServer.start();
        ((JavaMailSenderImpl) mailSender).setPort(smtpPort);
        assertEquals(0, mailServer.getReceivedMessages().length);
        sshCall((is, os) -> {
            write(os, "help | m anand@test.com");
            verifyResponse(is, "Output response sent to anand@test.com");
            assertTrue(mailServer.waitForIncomingEmail(5000, 1));
            MimeMessage message = mailServer.getReceivedMessages()[0];
            try {
                assertEquals("help", message.getSubject());
            } catch (MessagingException ex) {
                fail("Messaging exception");
            } finally {
                try {
                    mailServer.purgeEmailFromAllMailboxes();
                } catch (FolderException ex) {
                    // Ignore
                } finally {
                    mailServer.stop();
                }
            }
        });
    }

    @Test
    public void testMailProcessorFail() {
        sshCall((is, os) -> {
            write(os, "help | m anand@test.com");
            verifyResponse(is, "Error sending mail, please check logs for more info");
        });
    }

    @Test
    public void testHighlightProcessor() {
        sshCall((is, os) -> {
            String format = "\r\n%-35s%s"; // must be same with usageInfoFormat in SshdShellProperties.java
            write(os, "help | h <emailId>");
            StringBuilder sb = new StringBuilder("Supported Commands");
            for (int i = 0; i < 19; i++) {
                sb.append(format);
            }
            sb.append("\r\nSupported post processors for output")
                    .append(String.format(Locale.ENGLISH, format, "h <arg>",
                            "Highlights <arg> in response output of command execution"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | h exit"))
                    .append(String.format(Locale.ENGLISH, "\r\n%-44s%s", "m \u001B[43m<emailId>\u001B[0m",
                            "Send response output of command execution to \u001B[43m<emailId>\u001B[0m"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | m bob@hope.com"));
            verifyResponse(is, String.format(Locale.ENGLISH, sb.toString(), "auditEvents", "Event auditing",
                    "autoConfigurationReport", "Autoconfiguration report", "beans", "List beans",
                    "configurationPropertiesReport", "Configuration properties report", "dummy", "dummy description",
                    "environment", "Environment details", "exit", "Exit shell", "health", "System health info", "help",
                    "Show list of help commands", "iae", "throws IAE", "info", "System status", "loggers",
                    "Logging configuration", "metrics", "Metrics operations", "requestMapping",
                    "Request mapping information", "shutdown", "Shutdown application", "status", "System status",
                    "test", "test description", "threadDump", "Print thread dump", "traces", "Trace information"));
        });
    }

    @Test
    public void testInvalidCommand() {
        sshCall((is, os) -> {
            write(os, "help | h bob@hope.com | m bob@hope.com");
            verifyResponse(is, "Invalid command");
        });
    }

    @Test
    public void testInvalidCommand2() {
        sshCall((is, os) -> {
            write(os, "help | x");
            verifyResponse(is, "Invalid command");
        });
    }

    @Test
    public void testAuditEventsCommandNullArg() {
        sshCall((is, os) -> {
            write(os, "auditEvents list");
            verifyResponse(is, "Usage: auditEvents list {\"principal\":\"<user>\",\"after\":\"<yyyy-MM-dd HH:mm>\","
                    + "\"type\":\"<type>\"}");
        });
    }
    
    @Test
    public void testAuditEventsCommandInvalidJson() {
        sshCall((is, os) -> {
            write(os, "auditEvents list xxx");
            verifyResponse(is, "Expected valid json as argument");
        });
    }
    
    @Test
    public void testAuditEventsCommand() {
        sshCall((is, os) -> {
            write(os, "auditEvents list {}");
            verifyResponse(is, "{\r\n  \"events\" : [ ]\r\n}");
        });
    }
    
    @Test
    public void testAutoConfigurationReportCommand() {
        sshCall((is, os) -> {
            write(os, "autoConfigurationReport");
            verifyResponse(is, "{\r\n  \"positiveMatches\" : {");
        });
    }
    
    @Test
    public void testBeansCommand() {
        sshCall((is, os) -> {
            write(os, "beans");
            verifyResponse(is, "{\r\n  \"id\" : ");
        });
    }
    
    @Test
    public void testConfigurationPropertiesReportCommand() {
        sshCall((is, os) -> {
            write(os, "configurationPropertiesReport");
            verifyResponse(is, "{\r\n  \"beans\" : {");
        });
    }
    
    @Test
    public void testEnvironmentPatternCommand() {
        sshCall((is, os) -> {
           write(os, "environment pattern");
           verifyResponse(is, "{\r\n  \"activeProfiles\"");
        });
    }
    
    @Test
    public void testEnvironmentEntryCommandValidArg() {
        sshCall((is, os) -> {
            write(os, "environment entry server.port");
            verifyResponse(is, "{\r\n  \"property\"");
        });
    }
    
    @Test
    public void testEnvironmentEntryCommandInvalidArg() {
        sshCall((is, os) -> {
            write(os, "environment entry");
            verifyResponse(is, "Usage: environment entry <stringToMatch>");
        });
    }
    
    @Test
    public void testHealthCommand() {
        int smtpPort = SocketUtils.findAvailableTcpPort();
        ServerSetup setup = new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        GreenMail mailServer = new GreenMail(setup);
        mailServer.start();
        ((JavaMailSenderImpl) mailSender).setPort(smtpPort);
        sshCall((is, os) -> {
            write(os, "health");
            verifyResponse(is, "{\r\n  \"status\" : \"UP\"");
            mailServer.stop();
        });
    }
    
    @Test
    public void testInfoCommand() {
        sshCall((is, os) -> {
            write(os, "info");
            verifyResponse(is, "{ }");
        });
    }
    
    @Test
    public void testLoggersCommandInfo() {
        sshCall((is, os) -> {
            write(os, "loggers info");
            verifyResponse(is, "{\r\n  \"levels\" : [ \"OFF\", \"ERROR\", \"WARN\", \"INFO\", \"DEBUG\", \"TRACE\" ]");
        });
    }
    
    @Test
    public void testLoggersCommandLoggerLevelsNullArg() {
        sshCall((is, os) -> {
            write(os, "loggers level");
            verifyResponse(is, "Usage: loggers level <loggerName>");
        });
    }
    
    @Test
    public void testLoggersCommandLoggerLevelsValidArg() {
        sshCall((is, os) -> {
            write(os, "loggers level ROOT");
            verifyResponse(is, "{\r\n  \"configuredLevel\" : \"INFO\",\r\n  \"effectiveLevel\" : \"INFO\"\r\n}");
        });
    }
    
    @Test
    public void testLoggersCommandConfigureNullArg() {
        sshCall((is, os) -> {
            write(os, "loggers configure");
            verifyResponse(is, "Usage: loggers configure {\"name\":\"<loggerName>\",\"configuredLevel\":"
                    + "\"<Select from TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF>\"}");
        });
    }
    
    @Test
    public void testLoggersCommandConfigureValidArg() {
        sshCall((is, os) -> {
            write(os, "loggers configure {\"name\":\"ROOT\",\"configuredLevel\":\"INFO\"}");
            verifyResponse(is, "Changed log level for ROOT to INFO");
        });
    }
    
    @Test
    public void testLoggersCommandConfigureInvalidJson() {
        sshCall((is, os) -> {
            write(os, "loggers configure {}");
            verifyResponse(is, "Expected valid json as argument");
        });
    }
    
    @Test
    public void testMetricsCommandListNames() {
        sshCall((is, os) -> {
            write(os, "metrics listNames");
            verifyResponse(is, "{\r\n  \"names\" : [");
        });
    }
    
    @Test
    public void testMetricsCommandNullMetricName() {
        sshCall((is, os) -> {
            write(os, "metrics metricName");
            verifyResponse(is, "Usage: metrics metricName {\"name\":\"<metricName>\",\"tags\":[\"<array of tags>\"]}");
        });
    }
    
    @Test
    public void testMetricsCommandValidMetricName() {
        sshCall((is, os) -> {
            write(os, "metrics metricName {\"name\":\"jvm.memory.used\"}");
            verifyResponse(is, "{\r\n  \"name\" : \"jvm.memory.used");
        });
    }
    
    @Test
    public void testMetricsInvalidJson() {
        sshCall((is, os) -> {
            write(os, "metrics metricName {}");
            verifyResponse(is, "Expected valid json as argument");
        });
    }
    
    @Test
    public void testRequestMappingCommand() {
        sshCall((is, os) -> {
            write(os, "requestMapping");
            verifyResponse(is, "{\r\n  \"/webjars/**\"");
        });
    }
    
    @Test
    @DirtiesContext
    public void testShutdownCommand() {
        sshCall((is, os) -> {
            write(os, "shutdown");
            verifyResponse(is, "{\r\n  \"message\" : \"Shutting down, bye...\"\r\n}");
        });
    }
    
    @Test
    public void testStatusCommand() {
        int smtpPort = SocketUtils.findAvailableTcpPort();
        ServerSetup setup = new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        GreenMail mailServer = new GreenMail(setup);
        mailServer.start();
        ((JavaMailSenderImpl) mailSender).setPort(smtpPort);
        sshCall((is, os) -> {
            write(os, "status");
            verifyResponse(is, "{\r\n  \"status\" : \"UP\"\r\n}");
            mailServer.stop();
        });
    }
    
    @Test
    public void testThreadDumpCommand() {
        sshCall((is, os) -> {
            write(os, "threadDump");
            verifyResponse(is, "{\r\n  \"threads\" : [ {");
        });
    }
    
    @Test
    public void testTracesCommand() {
        sshCall((is, os) -> {
            write(os, "traces");
            verifyResponse(is, "{\r\n  \"traces\" : [ ]\r\n}");
        });
    }
}
