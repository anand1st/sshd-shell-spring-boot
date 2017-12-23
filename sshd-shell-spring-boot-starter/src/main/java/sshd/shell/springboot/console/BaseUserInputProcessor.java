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
package sshd.shell.springboot.console;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;

/**
 *
 * @author anand
 */
public abstract class BaseUserInputProcessor {

    @Lazy
    @Autowired
    private Map<String, Map<String, CommandExecutableDetails>> commandMap;
    
    public abstract Optional<UsageInfo> getUsageInfo();
    
    public abstract Pattern getPattern();
    
    public abstract void processUserInput(String userInput) throws InterruptedException, ShellException;
    
    protected final String processCommands(String userInput) throws InterruptedException, ShellException {
        String[] part = userInput.trim().split(" ", 3); // Three parts: command, subcommand, arg
        Collection<String> userRoles = getUserRoles(part[0]);
        return part.length < 2 ? handleSingleTokenUserInput(part[0], userRoles)
                : handleUserInputWithMoreTokens(part, userRoles);
    }
    
    public String[] splitAndValidateCommand(String userInput, String regex, int expectedNumberOfParts) throws ShellException {
        String[] part = userInput.split(regex);
        if (part.length != expectedNumberOfParts) {
            throw new ShellException("Invalid command");
        }
        return part;
    }
    
    private Collection<String> getUserRoles(String command) throws ShellException {
        Collection<String> userRoles = SshSessionContext.<Collection<String>>get(Constants.USER_ROLES);
        Map<String, CommandExecutableDetails> commandExecutables = getCommandExecutables(command);
        CommandExecutableDetails ced = commandExecutables.get(Constants.EXECUTE);
        validateExecutableWithUserRole(ced, userRoles);
        return userRoles;
    }

    private Map<String, CommandExecutableDetails> getCommandExecutables(String command) throws ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(command);
        if (Objects.isNull(commandExecutables)) {
            throw new ShellException(TerminalProcessor.UNSUPPORTED_COMMANDS_MESSAGE);
        }
        return commandExecutables;
    }

    private void validateExecutableWithUserRole(CommandExecutableDetails ced, Collection<String> userRoles) throws
            ShellException {
        if (!ced.matchesRole(userRoles)) {
            throw new ShellException("Permission denied");
        }
    }

    private String handleSingleTokenUserInput(String command, Collection<String> userRoles) throws InterruptedException {
        CommandExecutableDetails ced = commandMap.get(command).get(Constants.EXECUTE);
        return Objects.isNull(ced.getCommandExecutor()) ? unknownSubcommandMessage(command, userRoles)
                : ced.executeWithArg(null);
    }

    private String unknownSubcommandMessage(String command, Collection<String> userRoles) {
        StringBuilder sb = new StringBuilder("Supported subcommand for ").append(command);
        commandMap.get(command).entrySet().stream()
                .filter(e -> !e.getKey().equals(Constants.EXECUTE) && e.getValue().matchesRole(userRoles))
                .forEach(e -> sb.append("\n\r").append(e.getKey()).append("\t\t")
                .append(e.getValue().getDescription()));
        return sb.toString();
    }

    private String handleUserInputWithMoreTokens(String[] part, Collection<String> userRoles) throws
            InterruptedException, ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(part[0]);
        if (!commandExecutables.containsKey(part[1])) {
            throw new ShellException("Unknown subcommand '" + part[1] + "'. Type '" + part[0]
                    + "' for supported subcommands");
        }
        CommandExecutableDetails ced = commandMap.get(part[0]).get(part[1]);
        validateExecutableWithUserRole(ced, userRoles);
        return ced.executeWithArg(part.length == 2 ? null : part[2]);
    }
}
