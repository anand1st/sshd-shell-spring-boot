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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.ImageBanner;
import org.springframework.boot.ResourceBanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 * @author anand
 */
@Component(value = "__shellBanner")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE, onConstructor = @__(@Autowired))
class ShellBanner implements Banner {

    private static final String[] SUPPORTED_IMAGES = {"gif", "jpg", "png"};
    private final List<Banner> banners = new ArrayList<>();
    private final Environment environment;

    @PostConstruct
    void init() {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String location = environment.getProperty("banner.image.location");
        if (StringUtils.hasLength(location)) {
            addBanner(resourceLoader, location, resource -> new ImageBanner(resource));
        } else {
            for (String ext : SUPPORTED_IMAGES) {
                addBanner(resourceLoader, "banner." + ext, resource -> new ImageBanner(resource));
            }
        }
        addBanner(resourceLoader, environment.getProperty("banner.location", "banner.txt"),
                resource -> new ResourceBanner(resource));
    }

    private void addBanner(ResourceLoader resourceLoader, String bannerResourceName,
            Function<Resource, Banner> function) {
        Resource bannerResource = resourceLoader.getResource(bannerResourceName);
        if (bannerResource.exists()) {
            banners.add(new BannerDecorator(function.apply(bannerResource)));
        }
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        banners.forEach(banner -> banner.printBanner(environment, sourceClass, out));
    }

    /**
     * Banner decorator class that replaces new line with newline and carriage return to print image/text outputs
     * correctly on shell.
     */
    @lombok.AllArgsConstructor
    private static class BannerDecorator implements Banner {

        private final Banner decorator;
        
        @Override
        public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            decorator.printBanner(environment, sourceClass, new PrintStream(output));
            out.print(new String(output.toByteArray(), StandardCharsets.UTF_8).replace("\n", "\n\r"));
        }
    }
}
