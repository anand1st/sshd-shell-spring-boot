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
package sshd.shell.springboot.autoconfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.util.CollectionUtils;
import sshd.shell.springboot.ShellException;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
public class CommandExecutableDetails {

    private final Set<String> roles;
    private final String command;
    @lombok.Getter
    private final String description;
    @lombok.Getter
    private final CommandExecutor commandExecutor;

    CommandExecutableDetails(SshdShellCommand command, Set<String> roles, CommandExecutor commandExecutor) {
        this.roles = Collections.unmodifiableSet(roles);
        this.command = command.value();
        this.description = command.description();
        this.commandExecutor = commandExecutor;
    }

    public boolean matchesRole(Collection<String> userRoles) {
        if (roles.contains("*") || userRoles.contains("*")) {
            return true;
        }
        return CollectionUtils.containsAny(roles, userRoles);
    }

    public String executeWithArg(String arg) throws InterruptedException, ShellException {
        return commandExecutor.get(arg);
    }

    @Override
    public String toString() {
        return new StringBuilder("Command: ").append(command).append(", description: ").append(description)
                .append(", roles: ").append(roles).toString();
    }
}
