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
import org.springframework.boot.actuate.health.HealthEndpoint;
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
@ConditionalOnBean(HealthEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.health.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "health", description = "System health info")
public final class HealthCommand extends AbstractSystemCommand {

    private final HealthEndpoint healthEndpoint;

    HealthCommand(@Value("${sshd.system.command.roles.health}") String[] systemRoles, HealthEndpoint healthEndpoint) {
        super(systemRoles);
        this.healthEndpoint = healthEndpoint;
    }

    @SshdShellCommand(value = "info", description = "Health info for all components")
    public String healthInfo(String arg) {
        return JsonUtils.asJson(healthEndpoint.health());
    }

    @SshdShellCommand(value = "component", description = "Health for specified components")
    public String healthForComponent(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: health component <component1>,<component2> and so on";
        }
        String[] components = StringUtils.trimAllWhitespace(arg).split(",");
        return JsonUtils.asJson(healthEndpoint.healthForPath(components));
    }
}
