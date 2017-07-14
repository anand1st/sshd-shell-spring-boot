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
package demo;

import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "admin", description = "Admin functionality. Type 'admin' for supported subcommands",
        roles = "ADMIN")
public class AdminCommand {
    
    @SshdShellCommand(value = "manage", description = "Manage task. Usage: admin manage <arg>", roles = "ADMIN")
    public String manage(String arg) {
        return arg + " has been managed by admin";
    }
}
