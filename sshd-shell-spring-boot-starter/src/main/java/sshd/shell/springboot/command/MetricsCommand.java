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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.util.JsonUtils;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnBean(MetricsEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.metrics.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "metrics", description = "Metrics operations")
public final class MetricsCommand extends AbstractSystemCommand {

    private final MetricsEndpoint metricsEndpoint;

    MetricsCommand(@Value("${sshd.system.command.roles.metrics}") String[] systemRoles,
            MetricsEndpoint metricsEndpoint) {
        super(systemRoles);
        this.metricsEndpoint = metricsEndpoint;
    }

    @SshdShellCommand(value = "listNames", description = "List names of all metrics")
    public String listNames(String arg) {
        return JsonUtils.asJson(metricsEndpoint.listNames());
    }

    @SshdShellCommand(value = "metricName", description = "List metric name")
    public String metricName(String arg) {
        if (!StringUtils.hasText(arg)) {
            return "Usage: metrics metricName {\"name\":\"<metricName>\",\"tags\":[\"<array of tags>\"]}";
        }
        return CommandUtils.process(() -> {
            MetricTags mt = JsonUtils.stringToObject(arg, MetricTags.class);
            return JsonUtils.asJson(metricsEndpoint.metric(mt.name, mt.tags));
        });
    }

    private static class MetricTags {

        @JsonProperty(required = true)
        public String name;
        @JsonIgnoreProperties(ignoreUnknown = true)
        public List<String> tags;
    }
}
