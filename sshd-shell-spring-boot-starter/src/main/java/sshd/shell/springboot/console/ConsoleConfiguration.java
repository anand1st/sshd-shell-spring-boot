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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jline.builtins.Completers;
import static org.jline.builtins.Completers.TreeCompleter.node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;

/**
 *
 * @author anand
 */
@Configuration
@ConditionalOnProperty(name = "sshd.shell.enabled", havingValue = "true")
class ConsoleConfiguration {

    @Autowired
    private SshdShellProperties properties;
    @Autowired
    private Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands;
    @Autowired
    private List<BaseUserInputProcessor> userInputProcessors;

    @Bean
    TerminalProcessor terminalProcessor() {
        List<Completers.TreeCompleter.Node> nodes = new ArrayList<>();
        sshdShellCommands.entrySet().stream().forEach(entry -> {
            Object[] subCommands = entry.getValue().keySet().stream().filter(s -> !s.equals(Constants.EXECUTE))
                    .toArray(Object[]::new);
            nodes.add(subCommands.length == 0 ? node(entry.getKey()) : node(entry.getKey(), node(subCommands)));
        });
        return new TerminalProcessor(properties.getShell(), new Completers.TreeCompleter(nodes), userInputProcessors);
    }
}
