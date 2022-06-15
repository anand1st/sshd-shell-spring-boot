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

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.ShellException;
import sshd.shell.springboot.util.Assert;

/**
 *
 * @author anand
 */
@Component("__defaultUserInputProcessor")
@Order(Integer.MAX_VALUE)
class DefaultUserInputProcessor extends BaseUserInputProcessor {

    private final String[] bannedSymbols = {"|"};

    @Override
    public Optional<UsageInfo> getUsageInfo() {
        return Optional.<UsageInfo>empty();
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile(".+");
    }

    @Override
    public void processUserInput(String userInput) throws InterruptedException, ShellException {
        for (String bannedSymbol : bannedSymbols) {
            Assert.isTrue(!userInput.contains(bannedSymbol), "Invalid command");
        }
        ConsoleIO.writeOutput(processCommands(userInput));
    }
}
