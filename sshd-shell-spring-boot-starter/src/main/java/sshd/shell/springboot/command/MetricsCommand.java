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
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.console.ConsoleIO;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(MetricsEndpoint.class)
@SshdShellCommand(value = "metrics", description = "Metrics operations")
@lombok.extern.slf4j.Slf4j
public class MetricsCommand {

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @SshdShellCommand(value = "listNames", description = "List names of all metrics")
    public String listNames(String arg) {
        return ConsoleIO.asJson(metricsEndpoint.listNames());
    }

    @SshdShellCommand(value = "metricName", description = "List metric name")
    public String metricName(String arg) {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: metrics metricName {\"name\":\"<metricName>\",\"tags\":[\"<array of tags>\"]}";
        }
        try {
            MetricTags mt = ConsoleIO.stringToObject(arg, MetricTags.class);
            return ConsoleIO.asJson(metricsEndpoint.metric(mt.name, mt.tags));
        } catch (IOException ex) {
            log.warn("Invalid json", ex);
            return "Expected valid json as argument";
        }
    }
    
    private static class MetricTags {
        
        @JsonProperty(required = true)
        public String name;
        @JsonIgnoreProperties(ignoreUnknown = true)
        public List<String> tags;
    }
}
