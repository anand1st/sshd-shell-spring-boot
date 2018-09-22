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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(ThreadDumpEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.threaddump.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "threadDump", description = "Print thread dump")
public final class ThreadDumpCommand {
    
    @Autowired
    private ThreadDumpEndpoint threadDumpEndpoint;
    
    public String threadDump(String arg) {
        return JsonUtils.asJson(threadDumpEndpoint.threadDump());
    }
}
