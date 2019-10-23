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
package sshd.shell.springboot.command;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnAvailableEndpoint(endpoint = MappingsEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.mappings.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "mappings", description = "List http request mappings")
public final class MappingsCommand extends AbstractSystemCommand {

    private final MappingsEndpoint mappingsEndpoint;

    MappingsCommand(@Value("${sshd.system.command.roles.mappings}") String[] systemRoles,
            MappingsEndpoint mappingsEndpoint) {
        super(systemRoles);
        this.mappingsEndpoint = mappingsEndpoint;
    }

    public String mappings(String arg) {
        return JsonUtils.asJson(mappingsEndpoint.mappings());
    }
}
