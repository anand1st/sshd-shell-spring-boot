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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import sshd.shell.springboot.ShellException;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;
import sshd.shell.springboot.util.Assert;

/**
 *
 * @author anand
 */
public abstract class BaseUserInputProcessor {

    @Lazy
    @Autowired
    private Map<String, Map<String, CommandExecutableDetails>> commandMap;
    @Autowired
    private SshdShellProperties props;

    public abstract Optional<UsageInfo> getUsageInfo();

    public abstract Pattern getPattern();

    public abstract void processUserInput(String userInput) throws InterruptedException, ShellException;

    protected final String processCommands(String userInput) throws InterruptedException, ShellException {
        String[] inputTokens = userInput.trim().split(" ", 3); // Three parts: command, subcommand, arg
        String command = inputTokens[0];
        Collection<String> userRoles = getValidatedUserRolesForCommand(command);
        return inputTokens.length < 2
                ? handleCommandOnlyUserInput(command, userRoles)
                : handleUserInputWithMoreTokens(inputTokens, userRoles);
    }

    protected String[] splitAndValidateCommand(String userInput, String regex, int expectedNumberOfTokens)
            throws ShellException {
        String[] tokens = userInput.split(regex);
        Assert.isTrue(tokens.length == expectedNumberOfTokens, "Invalid command");
        return tokens;
    }

    private Collection<String> getValidatedUserRolesForCommand(String command) throws ShellException {
        Collection<String> userRoles = SshSessionContext.<Collection<String>>get(Constants.USER_ROLES);
        CommandExecutableDetails ced = getExecutableForCommand(command);
        Assert.isTrue(ced.matchesRole(userRoles), "Permission denied");
        return userRoles;
    }

    private CommandExecutableDetails getExecutableForCommand(String command) throws ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(command);
        Assert.isNotNull(commandExecutables, TerminalProcessor.UNSUPPORTED_COMMANDS_MESSAGE);
        return commandExecutables.get(Constants.EXECUTE);
    }

    private String handleCommandOnlyUserInput(String command, Collection<String> userRoles) throws
            InterruptedException, ShellException {
        CommandExecutableDetails ced = commandMap.get(command).get(Constants.EXECUTE);
        return Objects.isNull(ced.getCommandExecutor())
                ? unknownSubcommandMessage(command, userRoles)
                : ced.executeWithArg(null);
    }

    private String unknownSubcommandMessage(String command, Collection<String> userRoles) {
        StringBuilder sb = new StringBuilder("Supported subcommand for ").append(command);
        commandMap.get(command).entrySet().stream()
                .filter(e -> !e.getKey().equals(Constants.EXECUTE) && e.getValue().matchesRole(userRoles))
                .forEach(e -> sb.append(String.format(Locale.ENGLISH, props.getShell().getText().getUsageInfoFormat(),
                e.getKey(), e.getValue().getDescription())));
        return sb.toString();
    }

    private String handleUserInputWithMoreTokens(String[] tokens, Collection<String> userRoles)
            throws InterruptedException, ShellException {
        String command = tokens[0];
        String subCommand = tokens[1];
        CommandExecutableDetails ced = getSubCommand(command, subCommand);
        Assert.isTrue(ced.matchesRole(userRoles), "Permission denied");
        return ced.executeWithArg(getArgument(tokens));
    }

    private CommandExecutableDetails getSubCommand(String command, String subCommand) throws ShellException {
        Map<String, CommandExecutableDetails> commandExecutables = commandMap.get(command);
        Assert.isTrue(commandExecutables.containsKey(subCommand),
                String.format("Unknown subcommand '%s'. Type '%s' for supported subcommands", subCommand, command));
        return commandMap.get(command).get(subCommand);
    }

    private String getArgument(String[] tokens) {
        return tokens.length == 2
                ? null
                : tokens[2];
    }
}
