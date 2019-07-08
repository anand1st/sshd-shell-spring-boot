/*
 * Copyright 2019 anand.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author anand
 */
public abstract class AbstractSystemCommand {

    private final Set<String> systemRoles;

    public AbstractSystemCommand(String[] systemRoles) {
        this.systemRoles = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(systemRoles)));
    }

    public final Set<String> getSystemRoles() {
        return systemRoles;
    }
}
