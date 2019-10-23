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

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.liquibase.LiquibaseEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(SpringLiquibase.class)
@ConditionalOnAvailableEndpoint(endpoint = LiquibaseEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.liquibase.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "liquibase", description = "Liquibase database migration details (if applicable)")
public final class LiquibaseCommand extends AbstractSystemCommand {

    private final LiquibaseEndpoint liquibaseEndpoint;

    LiquibaseCommand(@Value("${management.endpoint.liquibase.enabled}") String[] systemRoles,
            LiquibaseEndpoint liquibaseEndpoint) {
        super(systemRoles);
        this.liquibaseEndpoint = liquibaseEndpoint;
    }

    public String liquibase(String arg) {
        return JsonUtils.asJson(liquibaseEndpoint.liquibaseBeans());
    }
}
