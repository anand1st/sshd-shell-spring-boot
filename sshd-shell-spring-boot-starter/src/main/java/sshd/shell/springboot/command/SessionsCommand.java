/*
 * Copyright 2019 anand.
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

import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.session.SessionsEndpoint;
import org.springframework.boot.actuate.session.SessionsEndpoint.SessionDescriptor;
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
@ConditionalOnBean(SessionsEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.sessions.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "sessions", description = "Sessions management")
public final class SessionsCommand extends AbstractSystemCommand {

    private final SessionsEndpoint sessionsEndpoint;

    SessionsCommand(@Value("${sshd.system.command.roles.sessions}") String[] systemRoles,
            SessionsEndpoint sessionsEndpoint) {
        super(systemRoles);
        this.sessionsEndpoint = sessionsEndpoint;
    }

    @SshdShellCommand(value = "username", description = "Find sessions given username")
    public String findSessionsByUsername(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: sessions username <username>";
        }
        return JsonUtils.asJson(sessionsEndpoint.sessionsForUsername(arg));
    }

    @SshdShellCommand(value = "get", description = "Get session descriptor by session id")
    public String getSessionById(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: sessions get <sessionId>";
        }
        SessionDescriptor sessionsDescriptor = sessionsEndpoint.getSession(arg);
        return Objects.isNull(sessionsDescriptor)
                ? "No such sessionId"
                : JsonUtils.asJson(sessionsDescriptor);
    }

    @SshdShellCommand(value = "delete", description = "Delete by session id")
    public String deleteSessionById(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: sessions delete <sessionId>";
        }
        sessionsEndpoint.deleteSession(arg);
        return "Session [" + arg + "] is deleted";
    }
}
