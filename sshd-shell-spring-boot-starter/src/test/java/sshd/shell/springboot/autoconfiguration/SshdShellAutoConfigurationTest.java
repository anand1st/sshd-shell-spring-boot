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
import org.awaitility.Duration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

    @Test
    public void testMappingsCommand() {
        sshCallShell((is, os) -> {
            write(os, "mappings");
            verifyResponseContains(is, "{\r\n  \"contexts\" : {");
        });
    }

    @Test
    public void testExitCommand() {
        sshCallShell((is, os) -> {
            write(os, "exit");
            verifyResponseContains(is, "Exiting shell");
        });
    }

    @Test
    public void testEmptyUserInput() {
        sshCallShell((is, os) -> {
            write(os, "");
            verifyResponseContains(is, "\r\r\n");
        });
    }

    @Test
    public void testIAECommand() {
        sshCallShell((is, os) -> {
            write(os, "iae");
            verifyResponseContains(is, "Error performing method invocation\r\r\nPlease check server logs for more "
                    + "information");
        });
    }

    @Test
    public void testUnsupportedCommand() {
        sshCallShell((is, os) -> {
            write(os, "xxx");
            verifyResponseContains(is, "Unknown command. Enter 'help' for a list of supported commands");
        });
    }

    @Test
    public void testUnsupportedSubCommand() {
        sshCallShell((is, os) -> {
            write(os, "test nonexistent");
            verifyResponseContains(is, "Unknown subcommand 'nonexistent'. Type 'test' for supported subcommands");
        });
    }

    @Test
    public void testSubcommand() {
        sshCallShell((is, os) -> {
            write(os, "test");
            String format = props.getShell().getText().getUsageInfoFormat();
            String response = new StringBuilder("Supported subcommand for test")
                    .append('\r').append(String.format(Locale.ENGLISH, format, "execute", "test execute"))
                    .append('\r').append(String.format(Locale.ENGLISH, format, "interactive", "test interactive"))
                    .append('\r').append(String.format(Locale.ENGLISH, format, "run", "test run"))
                    .toString();
            verifyResponseContains(is, response);
        });
    }

    @Test
    public void testHelp() {
        sshCallShell((is, os) -> {
            String format = "\r" + props.getShell().getText().getUsageInfoFormat();
            write(os, "help");
            StringBuilder sb = new StringBuilder("Supported Commands");
            for (int i = 0; i < 21; i++) {
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
                    "auditEvents", "Event auditing",
                    "beans", "List beans",
                    "caches", "Caches info",
                    "conditionsReport", "Conditions report",
                    "configurationPropertiesReport", "Configuration properties report",
                    "dummy", "dummy description",
                    "environment", "Environment details",
                    "exit", "Exit shell",
                    "health", "System health info",
                    "heapDump", "Heap dump command",
                    "help", "Show list of help commands",
                    "httpTrace", "Http trace information",
                    "iae", "throws IAE",
                    "info", "System status",
                    "loggers", "Logging configuration",
                    "mappings", "List http request mappings",
                    "metrics", "Metrics operations",
                    "scheduledTasks", "Scheduled tasks",
                    "shutdown", "Shutdown application",
                    "test", "test description",
                    "threadDump", "Print thread dump"));
        });
    }

    @Test
    public void testInteractive() {
        sshCallShell((is, os) -> {
            write(os, "test interactive", "anand");
            verifyResponseContains(is, "{\r\n  \"obj\" : \"anand\"\r\n}\r\nHi anand");
        });
    }

    @Test
    public void testMailProcessor() {
        int smtpPort = SocketUtils.findAvailableTcpPort();
        ServerSetup setup = new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        GreenMail mailServer = new GreenMail(setup);
        JavaMailSenderImpl jmsi = (JavaMailSenderImpl) mailSender;
        mailServer.setUser(jmsi.getUsername(), jmsi.getPassword());
        mailServer.start();
        jmsi.setPort(smtpPort);
        assertEquals(0, mailServer.getReceivedMessages().length);
        sshCallShell((is, os) -> {
            write(os, "help | m anand@test.com");
            verifyResponseContains(is, "Output response sent to anand@test.com", Duration.ONE_MINUTE);
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
        sshCallShell((is, os) -> {
            write(os, "help | m anand@test.com");
            verifyResponseContains(is, "Error sending mail, please check logs for more info");
        });
    }

    @Test
    public void testHighlightProcessor() {
        sshCallShell((is, os) -> {
            write(os, "test | h subcommand");
            String format = props.getShell().getText().getUsageInfoFormat();
            String response = new StringBuilder("Supported \u001B[43msubcommand\u001B[0m for test")
                    .append('\r').append(String.format(Locale.ENGLISH, format, "execute", "test execute"))
                    .append('\r').append(String.format(Locale.ENGLISH, format, "interactive", "test interactive"))
                    .append('\r').append(String.format(Locale.ENGLISH, format, "run", "test run"))
                    .toString();
            verifyResponseContains(is, response);
        });
    }

    @Test
    public void testInvalidCommand() {
        sshCallShell((is, os) -> {
            write(os, "help | h bob@hope.com | m bob@hope.com");
            verifyResponseContains(is, "Invalid command");
        });
    }

    @Test
    public void testInvalidCommand2() {
        sshCallShell((is, os) -> {
            write(os, "help | x");
            verifyResponseContains(is, "Invalid command");
        });
    }

    @Test
    public void testAuditEventsCommandNullArg() {
        sshCallShell((is, os) -> {
            write(os, "auditEvents list");
            verifyResponseContains(is, "Usage: auditEvents list {\"principal\":\"<user>\","
                    + "\"after\":\"<yyyy-MM-dd HH:mm>\",\"type\":\"<type>\"}");
        });
    }

    @Test
    public void testAuditEventsCommandInvalidJson() {
        sshCallShell((is, os) -> {
            write(os, "auditEvents list xxx");
            verifyResponseContains(is, "Expected valid json as argument");
        });
    }

    @Test
    public void testAuditEventsCommand() {
        sshCallShell((is, os) -> {
            write(os, "auditEvents list {}");
            verifyResponseContains(is, "{\r\n  \"events\" : [ ]\r\n}");
        });
    }

    @Test
    public void testBeansCommand() {
        sshCallShell((is, os) -> {
            write(os, "beans");
            verifyResponseContains(is, "{\r\n  \"contexts\" : {");
        });
    }

    @Test
    public void testConfigurationPropertiesReportCommand() {
        sshCallShell((is, os) -> {
            write(os, "configurationPropertiesReport");
            verifyResponseContains(is, "{\r\n  \"contexts\" : {");
        });
    }

    @Test
    public void testEnvironmentPatternCommand() {
        sshCallShell((is, os) -> {
            write(os, "environment pattern");
            verifyResponseContains(is, "{\r\n  \"activeProfiles\"");
        });
    }

    @Test
    public void testEnvironmentEntryCommandValidArg() {
        sshCallShell((is, os) -> {
            write(os, "environment entry server.port");
            verifyResponseContains(is, "{\r\n  \"property\"");
        });
    }

    @Test
    public void testEnvironmentEntryCommandInvalidArg() {
        sshCallShell((is, os) -> {
            write(os, "environment entry");
            verifyResponseContains(is, "Usage: environment entry <stringToMatch>");
        });
    }

    @Test
    public void testHealthInfoCommand() {
        int smtpPort = SocketUtils.findAvailableTcpPort();
        ServerSetup setup = new ServerSetup(smtpPort, null, ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        GreenMail mailServer = new GreenMail(setup);
        JavaMailSenderImpl jmsi = (JavaMailSenderImpl) mailSender;
        mailServer.setUser(jmsi.getUsername(), jmsi.getPassword());
        mailServer.start();
        jmsi.setPort(smtpPort);
        sshCallShell((is, os) -> {
            write(os, "health info");
            verifyResponseContains(is, "{\r\n  \"status\" : \"UP\"");
            mailServer.stop();
        });
    }

    @Test
    public void testHealthComponentEmpty() {
        sshCallShell((is, os) -> {
            write(os, "health component");
            verifyResponseContains(is, "Usage: health component <component>");
        });
    }

    @Test
    public void testHealthComponentDB() {
        sshCallShell((is, os) -> {
            write(os, "health component diskSpace");
            verifyResponseContains(is, "{\r\n  \"status\" : \"UP\"");
        });
    }

    @Test
    public void testHealthComponentInstanceEmpty() {
        sshCallShell((is, os) -> {
            write(os, "health componentInstance");
            verifyResponseContains(is, "Usage: health componentInstance {\"component\":\"<component>\",\"instance\":"
                    + "\"<instance>\"}");
        });
    }

    @Test
    public void testHealthComponentInstance() {
        sshCallShell((is, os) -> {
            write(os, "health componentInstance {\"component\":\"diskSpace\",\"instance\":\"xxx\"}");
            verifyResponseContains(is, "null");
        });
    }

    @Test
    public void testCachesListCommand() {
        sshCallShell((is, os) -> {
            write(os, "caches list");
            verifyResponseContains(is, "{\r\n  \"cacheManagers");
        });
    }

    @Test
    public void testCachesShowEmptyCommand() {
        sshCallShell((is, os) -> {
            write(os, "caches show");
            verifyResponseContains(is, "Usage: caches show {\"cache\":\"<cache>\", \"cacheManager\":"
                    + "\"<cacheManager>\"}");
        });
    }

    @Test
    public void testCachesShowCommand() {
        sshCallShell((is, os) -> {
            write(os, "caches show {\"cache\":\"test\"}");
            verifyResponseContains(is, "{\r\n  \"target\" : \"com.github.benmanes.caffeine");
        });
    }

    @Test
    public void testInfoCommand() {
        sshCallShell((is, os) -> {
            write(os, "info");
            verifyResponseContains(is, "{ }");
        });
    }

    @Test
    public void testLoggersCommandInfo() {
        sshCallShell((is, os) -> {
            write(os, "loggers info");
            verifyResponseContains(is, "{\r\n  \"levels\" : [ \"OFF\", \"ERROR\", \"WARN\", \"INFO\", \"DEBUG\", "
                    + "\"TRACE\" ]");
        });
    }

    @Test
    public void testLoggersCommandLoggerLevelsNullArg() {
        sshCallShell((is, os) -> {
            write(os, "loggers level");
            verifyResponseContains(is, "Usage: loggers level <loggerName>");
        });
    }

    @Test
    public void testLoggersCommandLoggerLevelsValidArg() {
        sshCallShell((is, os) -> {
            write(os, "loggers level ROOT");
            verifyResponseContains(is, "{\r\n  \"configuredLevel\" : \"INFO\",\r\n  \"effectiveLevel\" : "
                    + "\"INFO\"\r\n}");
        });
    }

    @Test
    public void testLoggersCommandConfigureNullArg() {
        sshCallShell((is, os) -> {
            write(os, "loggers configure");
            verifyResponseContains(is, "Usage: loggers configure {\"name\":\"<loggerName>\",\"configuredLevel\":"
                    + "\"<Select from TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF>\"}");
        });
    }

    @Test
    public void testLoggersCommandConfigureValidArg() {
        sshCallShell((is, os) -> {
            write(os, "loggers configure {\"name\":\"ROOT\",\"configuredLevel\":\"INFO\"}");
            verifyResponseContains(is, "Changed log level for ROOT to INFO");
        });
    }

    @Test
    public void testLoggersCommandConfigureInvalidJson() {
        sshCallShell((is, os) -> {
            write(os, "loggers configure {}");
            verifyResponseContains(is, "Expected valid json as argument");
        });
    }

    @Test
    public void testMetricsCommandListNames() {
        sshCallShell((is, os) -> {
            write(os, "metrics listNames");
            verifyResponseContains(is, "{\r\n  \"names\" : [");
        });
    }

    @Test
    public void testMetricsCommandNullMetricName() {
        sshCallShell((is, os) -> {
            write(os, "metrics metricName");
            verifyResponseContains(is, "Usage: metrics metricName {\"name\":\"<metricName>\","
                    + "\"tags\":[\"<array of tags>\"]}");
        });
    }

    @Test
    public void testMetricsCommandValidMetricName() {
        sshCallShell((is, os) -> {
            write(os, "metrics metricName {\"name\":\"jvm.memory.used\"}");
            verifyResponseContains(is, "{\r\n  \"name\" : \"jvm.memory.used");
        });
    }

    @Test
    public void testMetricsInvalidJson() {
        sshCallShell((is, os) -> {
            write(os, "metrics metricName {\"help\":\"invalid\"}");
            verifyResponseContains(is, "Expected valid json as argument");
        });
    }

    @DirtiesContext
    @Test
    public void testShutdownCommand() {
        sshCallShell((is, os) -> {
            write(os, "shutdown");
            verifyResponseContains(is, "{\r\n  \"message\" : \"Shutting down, bye...\"\r\n}");
        });
    }

    @Test
    public void testThreadDumpCommand() {
        sshCallShell((is, os) -> {
            write(os, "threadDump");
            verifyResponseContains(is, "{\r\n  \"threads\" : [ {");
        });
    }

    @Test
    public void testHttpTraceCommand() {
        sshCallShell((is, os) -> {
            write(os, "httpTrace");
            verifyResponseContains(is, "{\r\n  \"traces\" : [ ]\r\n}");
        });
    }

    @Test
    public void testConditionsReportCommand() {
        sshCallShell((is, os) -> {
            write(os, "conditionsReport");
            verifyResponseContains(is, "{\r\n  \"contexts\" : {");
        });
    }

    @Test
    public void testScheduledTasksCommand() {
        sshCallShell((is, os) -> {
            write(os, "scheduledTasks");
            verifyResponseContains(is, "{\r\n  \"cron\" :");
        });
    }

    @Test
    public void testHeapDumpNoArg() {
        sshCallShell((is, os) -> {
            write(os, "heapDump live");
            verifyResponseContains(is, "Usage: heapDump live <true|false>");
        });
    }

    @Test
    public void testHeapDumpWithSftpDisabled() {
        sshCallShell((is, os) -> {
            write(os, "heapDump live true");
            verifyResponseContains(is, "Resource can be found at ");
        });
    }
}
