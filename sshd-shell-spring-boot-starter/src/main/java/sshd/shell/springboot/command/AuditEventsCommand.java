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
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEventsEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(AuditEventRepository.class)
@ConditionalOnAvailableEndpoint(endpoint = AuditEventsEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.auditevents.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "auditEvents", description = "Event auditing")
public final class AuditEventsCommand extends AbstractSystemCommand {

    private final AuditEventsEndpoint auditEventsEndpoint;

    AuditEventsCommand(@Value("${sshd.system.command.roles.auditEvents}") String[] systemRoles,
            AuditEventsEndpoint auditEventsEndpoint) {
        super(systemRoles);
        this.auditEventsEndpoint = auditEventsEndpoint;
    }

    @SshdShellCommand(value = "list", description = "List events")
    public String auditEvents(String arg) {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: auditEvents list {\"principal\":\"<user>\",\"after\":\"<yyyy-MM-dd HH:mm>\","
                    + "\"type\":\"<type>\"}";
        }
        return CommandUtils.process(() -> {
            Event event = JsonUtils.stringToObject(arg, Event.class);
            return JsonUtils.asJson(auditEventsEndpoint.events(event.principal, event.after, event.type));
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Event {

        public String principal;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        public OffsetDateTime after;
        public String type;
    }
}
