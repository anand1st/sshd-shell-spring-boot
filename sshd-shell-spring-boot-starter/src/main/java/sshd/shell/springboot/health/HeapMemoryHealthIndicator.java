package sshd.shell.springboot.health;

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

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(HealthIndicator.class)
class HeapMemoryHealthIndicator implements HealthIndicator {

    private static final int MB = 1024 * 1024;
    
    @Override
    public Health health() {
        Runtime r = Runtime.getRuntime();
        return Health.unknown()
                .withDetail("used", (r.totalMemory() - r.freeMemory()) / MB)
                .withDetail("free", r.freeMemory() / MB)
                .withDetail("total", r.totalMemory() / MB)
                .withDetail("max", r.maxMemory() / MB)
                .build();
    }
}
