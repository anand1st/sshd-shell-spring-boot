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
package sshd.shell.springboot.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.jline.builtins.Completers.TreeCompleter;
import org.jline.builtins.Completers.TreeCompleter.Node;
import static org.jline.builtins.Completers.TreeCompleter.node;
import org.jline.reader.Completer;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;

/**
 *
 * @author anand
 */
class SshSessionFactory implements Factory<Command> {

    private final SshdShellProperties properties;
    private final Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands;
    private final Environment environment;
    private final Banner banner;
    private final Completer completer;

    SshSessionFactory(Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands, Environment environment,
            SshdShellProperties properties, Banner banner) {
        this.properties = properties;
        this.sshdShellCommands = sshdShellCommands;
        this.environment = environment;
        this.banner = banner;
        List<Node> nodes = new ArrayList<>();
        sshdShellCommands.entrySet().stream().forEach(entry -> {
            Object[] subCommands = entry.getValue().keySet().stream().filter(s -> !s.equals(Constants.EXECUTE))
                    .toArray(Object[]::new);
            nodes.add(subCommands.length == 0 ? node(entry.getKey()) : node(entry.getKey(), node(subCommands)));
        });
        this.completer = new TreeCompleter(nodes);
    }

    @Override
    public Command create() {
        return new SshSessionInstance(properties, sshdShellCommands, environment, banner, completer);
    }
}
