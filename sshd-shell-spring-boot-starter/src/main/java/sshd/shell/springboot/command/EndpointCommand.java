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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(Endpoint.class)
@SshdShellCommand(value = "endpoint", description = "Invoke actuator endpoints")
public final class EndpointCommand {

    private static final ObjectWriter WRITER = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private final Map<String, Endpoint<?>> endpoints;
    private final String listOfEndpoints;
    
    @Autowired
    EndpointCommand(List<Endpoint<?>> endpoints) {
        List<Endpoint<?>> sortedEndpoints = endpoints.stream()
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .sorted((o1, o2) -> Boolean.compare(o2.isEnabled(), o1.isEnabled()))
                .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder(String.format(Locale.ENGLISH, "%-16s%s%n", "Endpoints", "Is Enabled?"))
                .append("---------       -----------");
        sortedEndpoints.forEach(e -> sb.append(String.format(Locale.ENGLISH, "%n%-16s%s", e.getId(), e.isEnabled())));
        listOfEndpoints = sb.toString();
        this.endpoints = endpoints.stream().collect(Collectors.toMap(e -> e.getId(), Function.identity()));
    }

    @SshdShellCommand(value = "list", description = "List all actuator endpoints")
    public String listEndpoints(String arg) {
        return listOfEndpoints;
    }

    @SshdShellCommand(value = "invoke", description = "Invoke provided actuator endpoint")
    public String invoke(String arg) throws JsonProcessingException {
        if (StringUtils.isEmpty(arg) || !endpoints.containsKey(arg)) {
            return "Null or unknown endpoint\n" + listOfEndpoints + "\nUsage: endpoint invoke <endpoint>";
        }
        Endpoint<?> endpoint = endpoints.get(arg);
        if (!endpoint.isEnabled()) {
            return "Endpoint " + arg + " is not enabled";
        }
        return WRITER.writeValueAsString(endpoint.invoke());
    }
}
