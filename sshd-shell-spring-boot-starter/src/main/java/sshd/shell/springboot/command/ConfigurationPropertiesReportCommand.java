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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
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
@ConditionalOnClass(ConfigurationPropertiesReportEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.configprops.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "configurationPropertiesReport", description = "Configuration properties report")
public final class ConfigurationPropertiesReportCommand extends AbstractSystemCommand {

    private final ConfigurationPropertiesReportEndpoint confPropsReportEndpoint;

    ConfigurationPropertiesReportCommand(ConfigurationPropertiesReportEndpoint confPropsReportEndpoint,
            @Value("${sshd.system.command.roles.configurationPropertiesReport}") String[] systemRoles) {
        super(systemRoles);
        this.confPropsReportEndpoint = confPropsReportEndpoint;
    }

    public String configurationPropertiesReport(String arg) {
        return JsonUtils.asJson(confPropsReportEndpoint.configurationProperties());
    }
}
