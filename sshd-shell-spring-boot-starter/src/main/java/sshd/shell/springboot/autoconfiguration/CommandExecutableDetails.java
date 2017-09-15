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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author anand
 */
public class CommandExecutableDetails {
    
    private final Set<String> roles;
    @lombok.Getter
    private final String description;
    @lombok.Getter
    private final CommandExecutor commandExecutor;
    
    CommandExecutableDetails(SshdShellCommand command, CommandExecutor commandExecutor) {
        this.roles = Collections.unmodifiableSet(new HashSet<>(Arrays.<String>asList(command.roles())));
        this.description = command.description();
        this.commandExecutor = commandExecutor;
    }
    
    public boolean matchesRole(Collection<String> userRoles) {
        if (roles.contains("*") || userRoles.contains("*")) {
            return true;
        }
        return CollectionUtils.containsAny(roles, userRoles);
    }
    
    public String executeWithArg(String arg) throws InterruptedException {
        return commandExecutor.get(arg);
    }
}
