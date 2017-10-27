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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * Helper component to discover endpoints and facilitate the creation of said endpoint command by developer.
 * @author anand
 */
@Component
@ConditionalOnClass(Endpoint.class)
@lombok.extern.slf4j.Slf4j
public final class EndpointCommand {

    @Autowired
    EndpointCommand(ApplicationContext appCtx) {
        appCtx.getBeansWithAnnotation(Endpoint.class).entrySet().stream().forEachOrdered(entry -> {
            log.info("{} : {}", entry.getKey(), entry.getValue().getClass().getName());
            for (Method m : entry.getValue().getClass().getDeclaredMethods()) {
                if (m.isAnnotationPresent(ReadOperation.class) || m.isAnnotationPresent(WriteOperation.class)) {
                    log.info("\tOp: {}", m.getName());
                    for (Parameter p : m.getParameters()) {
                        log.info("\t\tParameter {}, {}", p.getName(), p.getType().getName());
                    }
                }
            }
        });
    }
}
