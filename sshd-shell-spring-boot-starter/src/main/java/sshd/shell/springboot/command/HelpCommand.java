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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@lombok.NoArgsConstructor(access = lombok.AccessLevel.PACKAGE)
@SshdShellCommand(value = Constants.HELP, description = "Show list of help commands")
public final class HelpCommand {

    @Autowired
    @Lazy
    private Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands;

    public String help(String arg) {
        StringBuilder sb = new StringBuilder("Supported Commands");
        Collection<String> roles = SshSessionContext.<Collection<String>>get(Constants.USER_ROLES);
        sshdShellCommands.entrySet().stream()
                .filter(e -> e.getValue().get(Constants.EXECUTE).matchesRole(roles))
                .forEachOrdered(e -> sb.append(formatString(e)));
        return sb.toString();
    }
    
    private String formatString(Map.Entry<String, Map<String, CommandExecutableDetails>> entry) {
        return String.format(Locale.ENGLISH, "%n%-16s%s", entry.getKey(),
                entry.getValue().get(Constants.EXECUTE).getDescription());
    }
}
