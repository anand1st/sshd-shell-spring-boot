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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(LoggersEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.loggers.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "loggers", description = "Logging configuration")
public final class LoggersCommand extends AbstractSystemCommand {

    private final LoggersEndpoint loggersEndpoint;

    LoggersCommand(@Value("${sshd.system.command.roles.loggers}") String[] systemRoles,
            LoggersEndpoint loggersEndpoint) {
        super(systemRoles);
        this.loggersEndpoint = loggersEndpoint;
    }

    @SshdShellCommand(value = "info", description = "Show logging info")
    public String info(String arg) {
        return JsonUtils.asJson(loggersEndpoint.loggers());
    }

    @SshdShellCommand(value = "level", description = "Show log levels for given logger name")
    public String loggerLevels(String arg) {
        return !StringUtils.hasText(arg)
                ? "Usage: loggers level <loggerName>"
                : JsonUtils.asJson(loggersEndpoint.loggerLevels(arg));
    }

    @SshdShellCommand(value = "configure", description = "Configure log level for logger name")
    public String configureLogLevel(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: loggers configure {\"name\":\"<loggerName>\",\"configuredLevel\":"
                    + "\"<Select from TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF>\"}";
        }
        return CommandUtils.process(() -> {
            LogConfig logConfig = JsonUtils.stringToObject(arg, LogConfig.class);
            loggersEndpoint.configureLogLevel(logConfig.name, logConfig.configuredLevel);
            return "Changed log level for " + logConfig.name + " to " + logConfig.configuredLevel.name();
        });
    }

    private static class LogConfig {

        @JsonProperty(required = true)
        public String name;
        @JsonProperty(required = true)
        public LogLevel configuredLevel;
    }
}
