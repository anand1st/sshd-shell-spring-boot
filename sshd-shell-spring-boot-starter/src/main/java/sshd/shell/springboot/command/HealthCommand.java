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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.CompositeHealthIndicatorConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(value = CompositeHealthIndicatorConfiguration.class)
@SshdShellCommand(value = "health", description = "Health of services")
public class HealthCommand {

    private static final Pattern HEALTH_INDICATOR_PATTERN = Pattern.compile("(.*?)HealthIndicator");
    private final String helpMessage;
    private final Map<String, HealthIndicator> healthIndicatorMap;
    private final ObjectWriter objectWriter = new ObjectMapper().writer();

    @Autowired
    public HealthCommand(List<HealthIndicator> healthIndicators) {
        healthIndicatorMap = healthIndicators.stream().collect(Collectors.toMap(healthIndicator -> {
            Matcher matcher = HEALTH_INDICATOR_PATTERN.matcher(healthIndicator.getClass().getSimpleName());
            Assert.isTrue(matcher.matches(), "HealthIndicator classes not matching pattern");
            return matcher.group(1).toLowerCase();
        }, Function.identity(), (v1, v2) -> v1, TreeMap::new));
        StringBuilder sb = new StringBuilder("Supported health indicators below:");
        healthIndicatorMap.keySet().forEach(key -> sb.append("\n\r\t").append(key));
        helpMessage = sb.append("\n\rUsage: health show <health indicator>").toString();
    }

    @SshdShellCommand(value = "show", description = "Display health services")
    public final String show(String arg) throws JsonProcessingException {
        if (StringUtils.isEmpty(arg)) {
            return helpMessage;
        }
        if (!healthIndicatorMap.containsKey(arg)) {
            return "Unsupported health indicator " + arg + "\n\r" + helpMessage;
        }
        Health health = healthIndicatorMap.get(arg).health();
        Map<String, Object> map = new LinkedHashMap<>();
        if (health.getStatus() != Status.UNKNOWN) {
            map.put("status", health.getStatus().getCode());
        }
        map.put(arg, health.getDetails());
        return objectWriter.writeValueAsString(map);
    }
}
