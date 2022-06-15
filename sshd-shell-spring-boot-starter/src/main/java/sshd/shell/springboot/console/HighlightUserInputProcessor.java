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

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.ShellException;
import sshd.shell.springboot.console.UsageInfo.Row;
import sshd.shell.springboot.util.Assert;

/**
 *
 * @author anand
 */
@Component
@Order(1)
class HighlightUserInputProcessor extends BaseUserInputProcessor {

    private final Pattern pattern = Pattern.compile(".+\\s?\\|\\s?h (.+)");

    @Override
    public Optional<UsageInfo> getUsageInfo() {
        return Optional.of(new UsageInfo(Arrays.<Row>asList(
                new Row("h <arg>", "Highlights <arg> in response output of command execution"),
                new Row("", "Example usage: help | h exit"))));
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public void processUserInput(String userInput) throws InterruptedException, ShellException {
        String textToHighlight = getHighlightedText(userInput);
        String[] tokens = splitAndValidateCommand(userInput, "\\|", 2);
        String commandExecution = tokens[0];
        String output = processCommands(commandExecution);
        ConsoleIO.writeOutput(output, textToHighlight);
    }

    private String getHighlightedText(String userInput) throws ShellException {
        Matcher matcher = pattern.matcher(userInput);
        Assert.isTrue(matcher.find(), "Unexpected error");
        return matcher.group(1).trim();
    }
}
