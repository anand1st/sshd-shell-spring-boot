/*
 * Copyright 2018 anand.
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

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.cache.CachesEndpoint;
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
@ConditionalOnClass(CachesEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.caches.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "caches", description = "Caches info")
@lombok.extern.slf4j.Slf4j
public final class CachesCommand extends AbstractSystemCommand {

    private final CachesEndpoint cachesEndpoint;

    CachesCommand(@Value("${sshd.system.command.roles.caches}") String[] systemRoles, CachesEndpoint cachesEndpoint) {
        super(systemRoles);
        this.cachesEndpoint = cachesEndpoint;
    }

    @SshdShellCommand(value = "list", description = "Cache info")
    public String cacheList(String arg) {
        return JsonUtils.asJson(cachesEndpoint.caches());
    }

    @SshdShellCommand(value = "show", description = "Cache info by cache and cacheManager")
    public String cache(String arg) {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: caches show {\"cache\":\"<cache>\", \"cacheManager\":\"<cacheManager>\"}";
        }
        return CommandUtils.process(() -> {
            Cache cache = JsonUtils.stringToObject(arg, Cache.class);
            return JsonUtils.asJson(cachesEndpoint.cache(cache.cache, cache.cacheManager));
        });
    }

    private static class Cache {

        public String cache;
        public String cacheManager;
    }
}
