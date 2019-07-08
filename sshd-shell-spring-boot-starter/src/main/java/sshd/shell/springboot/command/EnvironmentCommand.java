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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(EnvironmentEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.env.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "environment", description = "Environment details")
public final class EnvironmentCommand extends AbstractSystemCommand {

    private final EnvironmentEndpoint envEndpoint;

    EnvironmentCommand(@Value("${sshd.system.command.roles.environment}") String[] systemRoles,
            EnvironmentEndpoint envEndpoint) {
        super(systemRoles);
        this.envEndpoint = envEndpoint;
    }

    @SshdShellCommand(value = "pattern", description = "Get environment details with given pattern")
    public String withPattern(String arg) {
        return JsonUtils.asJson(envEndpoint.environment(arg));
    }

    @SshdShellCommand(value = "entry", description = "Get environment details with string to match")
    public String withEntry(String arg) {
        return StringUtils.isEmpty(arg)
                ? "Usage: environment entry <stringToMatch>"
                : JsonUtils.asJson(envEndpoint.environmentEntry(arg));
    }
}
