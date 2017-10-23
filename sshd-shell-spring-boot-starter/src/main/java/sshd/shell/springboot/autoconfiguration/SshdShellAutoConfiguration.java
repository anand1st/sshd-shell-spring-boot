/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;

/**
 *
 * @author anand
 */
@Configuration
@ComponentScan(basePackages = "sshd.shell.springboot")
@ConditionalOnProperty(name = "sshd.shell.enabled", havingValue = "true")
@EnableConfigurationProperties(SshdShellProperties.class)
@lombok.extern.slf4j.Slf4j
class SshdShellAutoConfiguration {

    @Autowired
    private ApplicationContext appContext;

    @Bean
    Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands() throws NoSuchMethodException,
            InterruptedException {
        Map<String, Map<String, CommandExecutableDetails>> sshdShellCommandsMap = new TreeMap<>();
        for (Map.Entry<String, Object> entry : appContext.getBeansWithAnnotation(SshdShellCommand.class).entrySet()) {
            loadSshdShellCommands(sshdShellCommandsMap, entry.getValue());
        }
        return Collections.unmodifiableMap(sshdShellCommandsMap.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> Collections.unmodifiableMap(e.getValue()), (v1, v2) -> {
                    throw new IllegalStateException();
                }, TreeMap::new)));
    }

    private void loadSshdShellCommands(Map<String, Map<String, CommandExecutableDetails>> sshdShellCommandsMap,
            Object obj) throws SecurityException, NoSuchMethodException, InterruptedException {
        Class<?> clazz = AopUtils.isAopProxy(obj) ? AopUtils.getTargetClass(obj) : obj.getClass();
        SshdShellCommand annotation = AnnotationUtils.findAnnotation(clazz, SshdShellCommand.class);
        Map<String, CommandExecutableDetails> map = getSupplierMap(annotation, sshdShellCommandsMap);
        loadSshdShellCommandSuppliers(clazz, annotation, map, obj);
    }

    private Map<String, CommandExecutableDetails> getSupplierMap(SshdShellCommand annotation,
            Map<String, Map<String, CommandExecutableDetails>> sshdShellCommandsMap) {
        Map<String, CommandExecutableDetails> map = sshdShellCommandsMap.get(annotation.value());
        if (!Objects.isNull(map)) {
            throw new IllegalArgumentException("Duplicate commands in different classes are not allowed");
        }
        sshdShellCommandsMap.put(annotation.value(), map = new TreeMap<>());
        return map;
    }

    private void loadSshdShellCommandSuppliers(Class<?> clazz, SshdShellCommand annotation,
            Map<String, CommandExecutableDetails> map, Object obj) throws NoSuchMethodException, SecurityException,
            InterruptedException {
        loadClassLevelCommandSupplier(clazz, annotation, map, obj);
        loadMethodLevelCommandSupplier(clazz, map, obj);
    }

    private void loadClassLevelCommandSupplier(Class<?> clazz, SshdShellCommand annotation,
            Map<String, CommandExecutableDetails> map, Object obj) throws SecurityException, NoSuchMethodException {
        log.debug("Loading class level command supplier for {}", clazz.getName());
        try {
            Method method = clazz.getDeclaredMethod(annotation.value(), String.class);
            log.debug("Adding default command method {}", method.getName());
            map.put(Constants.EXECUTE, getMethodSupplier(annotation, method, obj));
        } catch (NoSuchMethodException ex) {
            map.put(Constants.EXECUTE, new CommandExecutableDetails(annotation, null));
            log.debug("Does not contain default command method {}", ex.getMessage());
        }
    }

    private CommandExecutableDetails getMethodSupplier(SshdShellCommand annotation, Method method, Object obj) {
        return new CommandExecutableDetails(annotation, (arg) -> {
            try {
                return (String) method.invoke(obj, arg);
            } catch (InvocationTargetException ex) {
                if (ex.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) ex.getCause();
                } else {
                    return printAndGetErrorInfo(ex.getCause());
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                return printAndGetErrorInfo(ex);
            }
        });
    }

    private String printAndGetErrorInfo(Throwable ex) {
        log.error("Error performing method invocation", ex);
        return "Error performing method invocation\r\nPlease check server logs for more information";
    }

    private void loadMethodLevelCommandSupplier(Class<?> clazz, Map<String, CommandExecutableDetails> map, Object obj)
            throws NoSuchMethodException, SecurityException, InterruptedException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SshdShellCommand.class)) {
                log.debug("{}.#{} is marked with annotation {}", clazz.getName(), method.getName(),
                        SshdShellCommand.class.getName());
                SshdShellCommand command = method.getDeclaredAnnotation(SshdShellCommand.class);
                map.put(command.value(), getMethodSupplier(command, method, obj));
            }
        }
    }
}
