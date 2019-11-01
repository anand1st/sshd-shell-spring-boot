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

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.logging.LogFileWebEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(LogFileWebEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.logfile.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "logfile", description = "Application log file")
@lombok.extern.slf4j.Slf4j
public final class LogfileCommand extends AbstractSystemCommand {

    private final LogFileWebEndpoint logFileWebEndpoint;

    LogfileCommand(@Value("${sshd.system.command.roles.logfile}") String[] systemRoles,
            LogFileWebEndpoint logFileWebEndpoint) {
        super(systemRoles);
        this.logFileWebEndpoint = logFileWebEndpoint;
    }

    public String logfile(String arg) throws IOException {
        Resource logFileResource = logFileWebEndpoint.logFile();
        try {
            Path path = CommandUtils.sessionUserPathContainingZippedResource(logFileResource);
            return "Resource can be downloaded with SFTP/SCP at " + path.getFileName().toString();
        } catch (IllegalStateException ex) {
            log.warn(ex.getMessage());
            return "Resource can be found at " + logFileResource.getFile().getAbsolutePath();
        }
    }
}
