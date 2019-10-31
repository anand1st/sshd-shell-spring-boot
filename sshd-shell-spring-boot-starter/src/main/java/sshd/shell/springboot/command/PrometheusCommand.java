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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(PrometheusScrapeEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.prometheus.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "prometheus", description = "Produces formatted metrics for scraping by Prometheus server")
public final class PrometheusCommand extends AbstractSystemCommand {

    private final PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    PrometheusCommand(@Value("${sshd.system.command.roles.prometheus}") String[] systemRoles,
            PrometheusScrapeEndpoint prometheusScrapeEndpoint) {
        super(systemRoles);
        this.prometheusScrapeEndpoint = prometheusScrapeEndpoint;
    }

    public String prometheus(String arg) {
        return prometheusScrapeEndpoint.scrape();
    }
}
