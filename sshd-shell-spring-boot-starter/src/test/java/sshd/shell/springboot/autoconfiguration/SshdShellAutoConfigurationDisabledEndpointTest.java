/*
 * Copyright 2018 anand.
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

import java.util.Locale;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author anand
 */
@SpringBootTest(classes = ConfigTest.class, properties = {
    "management.endpoint.auditevents.enabled=false",
    "management.endpoint.beans.enabled=false",
    "management.endpoint.conditions.enabled=false",
    "management.endpoint.configprops.enabled=false",
    "management.endpoint.env.enabled=false",
    "management.endpoint.health.enabled=false",
    "management.endpoint.httptrace.enabled=false",
    "management.endpoint.info.enabled=false",
    "management.endpoint.loggers.enabled=false",
    "management.endpoint.mappings.enabled=false",
    "management.endpoint.metrics.enabled=false",
    "management.endpoint.scheduledtasks.enabled=false",
    "management.endpoint.shutdown.enabled=false",
    "management.endpoint.threaddump.enabled=false"
})
public class SshdShellAutoConfigurationDisabledEndpointTest extends AbstractSshSupport {
    
    @Test
    public void testHelp() {
        sshCallShell((is, os) -> {
            String format = "\r\n%-35s%s"; // must be same with usageInfoFormat in SshdShellProperties.java
            write(os, "help");
            StringBuilder sb = new StringBuilder("Supported Commands");
            for (int i = 0; i < 5; i++) {
                sb.append(format);
            }
            sb.append("\r\nSupported post processors for output")
                    .append(String.format(Locale.ENGLISH, format, "h <arg>",
                            "Highlights <arg> in response output of command execution"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | h exit"))
                    .append(String.format(Locale.ENGLISH, format, "m <emailId>",
                            "Send response output of command execution to <emailId>"))
                    .append(String.format(Locale.ENGLISH, format, "", "Example usage: help | m bob@hope.com"));
            verifyResponse(is, String.format(Locale.ENGLISH, sb.toString(),
                    "dummy", "dummy description",
                    "exit", "Exit shell",
                    "help", "Show list of help commands",
                    "iae", "throws IAE",
                    "test", "test description"));
        });
    }
}
