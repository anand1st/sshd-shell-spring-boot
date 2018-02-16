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
package sshd.shell.springboot.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.console.ConsoleIO;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(AuditEventsEndpoint.class)
@SshdShellCommand(value = "auditEvents", description = "Event auditing")
@lombok.extern.slf4j.Slf4j
public final class AuditEventsCommand {

    @Autowired
    private AuditEventsEndpoint auditEventsEndpoint;

    @SshdShellCommand(value = "list", description = "List events")
    public String auditEvents(String arg) {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: auditEvents list {\"principal\":\"<user>\",\"after\":\"<yyyy-MM-dd HH:mm>\","
                    + "\"type\":\"<type>\"}";
        }
        try {
            Event event = ConsoleIO.stringToObject(arg, Event.class);
            return ConsoleIO.asJson(auditEventsEndpoint.events(event.principal, event.after, event.type));
        } catch (IOException ex) {
            log.warn("Invalid json", ex);
            return "Expected valid json as argument";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Event {

        public String principal;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        public OffsetDateTime after;
        public String type;
    }
}
