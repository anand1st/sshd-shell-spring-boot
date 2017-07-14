/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import java.util.Map;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
class SshSessionFactory implements Factory<Command> {
    
    private final SshdShellProperties properties;
    private final Map<String, Map<String, CommandExecutableDetails>> commandMap;
    private final Environment environment;
    private final Banner shellBanner;

    @Override
    public Command create() {
        return new SshSessionInstance(properties, commandMap, environment, shellBanner);
    }
}
