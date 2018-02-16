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
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.console.ConsoleIO;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(LoggersEndpoint.class)
@SshdShellCommand(value = "loggers", description = "Logging configuration")
@lombok.extern.slf4j.Slf4j
public final class LoggersCommand {

    @Autowired
    private LoggersEndpoint loggersEndpoint;

    @SshdShellCommand(value = "info", description = "Show logging info")
    public String info(String arg) {
        return ConsoleIO.asJson(loggersEndpoint.loggers());
    }

    @SshdShellCommand(value = "level", description = "Show log levels for given logger name")
    public String loggerLevels(String arg) {
        return StringUtils.isEmpty(arg) ? "Usage: loggers level <loggerName>"
                : ConsoleIO.asJson(loggersEndpoint.loggerLevels(arg));
    }

    @SshdShellCommand(value = "configure", description = "Configure log level for logger name")
    public String configureLogLevel(String arg) {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: loggers configure {\"name\":\"<loggerName>\",\"configuredLevel\":"
                    + "\"<Select from TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF>\"}";
        }
        try {
            LogConfig logConfig = ConsoleIO.stringToObject(arg, LogConfig.class);
            loggersEndpoint.configureLogLevel(logConfig.name, logConfig.configuredLevel);
            return "Changed log level for " + logConfig.name + " to " + logConfig.configuredLevel.name();
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Invalid json", ex);
            return "Expected valid json as argument";
        }
    }

    private static class LogConfig {

        @JsonProperty(required = true)
        public String name;
        @JsonProperty(required = true)
        public LogLevel configuredLevel;
    }
}
