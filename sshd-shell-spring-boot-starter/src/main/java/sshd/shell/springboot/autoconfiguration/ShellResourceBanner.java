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
package sshd.shell.springboot.autoconfiguration;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
class ShellResourceBanner extends ResourceBanner {

    private final Resource resource;

    ShellResourceBanner(Resource resource) {
        super(resource);
        this.resource = resource;
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            String banner = StreamUtils.copyToString(this.resource.getInputStream(),
                    environment.getProperty("banner.charset", Charset.class, StandardCharsets.UTF_8))
                    .replace("\n", "\n\r");
            for (PropertyResolver resolver : getPropertyResolvers(environment, sourceClass)) {
                banner = resolver.resolvePlaceholders(banner);
            }
            out.println(banner);
        } catch (IOException ex) {
            log.warn("Banner not printable: " + this.resource + " (" + ex.getClass()
                    + ": '" + ex.getMessage() + "')", ex);
        }
    }
}
