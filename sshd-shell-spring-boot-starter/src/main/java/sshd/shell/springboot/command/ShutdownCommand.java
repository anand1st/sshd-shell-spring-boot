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
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnAvailableEndpoint(endpoint = ShutdownEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.shutdown.enabled", havingValue = "true", matchIfMissing = false)
@SshdShellCommand(value = "shutdown", description = "Shutdown application")
public final class ShutdownCommand extends AbstractSystemCommand {

    private final ShutdownEndpoint shutdownEndpoint;

    ShutdownCommand(@Value("${sshd.system.command.roles.shutdown}") String[] systemRoles,
            ShutdownEndpoint shutdownEndpoint) {
        super(systemRoles);
        this.shutdownEndpoint = shutdownEndpoint;
    }

    public String shutdown(String arg) {
        return JsonUtils.asJson(shutdownEndpoint.shutdown());
    }
}
