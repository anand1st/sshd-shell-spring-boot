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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.ImageBanner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

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

    public static final String HELP = "help";
    public static final String EXECUTE = "__execute";
    private static final String[] SUPPORTED_IMAGES = {"gif", "jpg", "png"};
    private static final String SUMMARY_HELP = "__summaryHelp";

    @Autowired
    private SshdShellProperties properties;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private Environment environment;
    private SshServer server;

    @PostConstruct
    void startServer() throws IOException, NoSuchMethodException, InterruptedException {
        if (Objects.isNull(properties.getShell().getPassword())) {
            properties.getShell().setPassword(UUID.randomUUID().toString());
            log.info("********** User password not set. Use following password to login: {} **********",
                    properties.getShell().getPassword());
        }
        server = SshServer.setUpDefaultServer();
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(properties.getShell().getHostKeyFile())));
        server.setPublickeyAuthenticator(Objects.isNull(properties.getShell().getPublicKeyFile())
                ? RejectAllPublickeyAuthenticator.INSTANCE
                : new AuthorizedKeysAuthenticator(new File(properties.getShell().getPublicKeyFile())));
        server.setHost(properties.getShell().getHost());
        server.setPasswordAuthenticator((username, password, session)
                -> username.equals(properties.getShell().getUsername())
                && password.equals(properties.getShell().getPassword()));
        server.setPort(properties.getShell().getPort());
        server.setShellFactory(new SshSessionFactory(properties, sshdShellCommands(), environment, shellBanner()));
        server.start();
        properties.getShell().setPort(server.getPort()); // In case server port is 0, a random port is assigned.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (IOException ex) {
                    log.error("Error shutting down SSH server", ex);
                }
            }
        });
        log.info("SSH server started on port {}", properties.getShell().getPort());
    }

    private Banner shellBanner() {
        Banners banners = new Banners();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String location = environment.getProperty("banner.image.location");
        if (StringUtils.hasLength(location)) {
            addBanner(resourceLoader, location, banners, resource -> new ImageBanner(resource));
        } else {
            for (String ext : SUPPORTED_IMAGES) {
                addBanner(resourceLoader, "banner." + ext, banners, resource -> new ImageBanner(resource));
            }
        }
        addBanner(resourceLoader, environment.getProperty("banner.location", "banner.txt"), banners,
                resource -> new ResourceBanner(resource));
        return banners;
    }

    private void addBanner(ResourceLoader resourceLoader, String bannerResourceName, Banners banners,
            Function<Resource, Banner> function) {
        Resource bannerResource = resourceLoader.getResource(bannerResourceName);
        if (bannerResource.exists()) {
            banners.addBanner(function.apply(bannerResource));
        }
    }

    @Bean
    Map<String, Map<String, CommandSupplier>> sshdShellCommands() throws NoSuchMethodException,
            InterruptedException {
        Map<String, Map<String, CommandSupplier>> sshdShellCommandsMap = new TreeMap<>();
        for (Map.Entry<String, Object> entry : appContext.getBeansWithAnnotation(SshdShellCommand.class).entrySet()) {
            loadSshdShellCommands(sshdShellCommandsMap, entry.getValue());
        }
        StringBuilder sb = new StringBuilder("Supported Commands");
        for (Map.Entry<String, Map<String, CommandSupplier>> entry : sshdShellCommandsMap.entrySet()) {
            sb.append("\n\r").append(entry.getKey()).append("\t\t")
                    .append(entry.getValue().remove(SUMMARY_HELP).get(null));
        }
        sshdShellCommandsMap.put(HELP, Collections.singletonMap(EXECUTE, arg -> sb.toString()));
        return sshdShellCommandsMap;
    }

    private void loadSshdShellCommands(Map<String, Map<String, CommandSupplier>> sshdShellCommandsMap, Object obj)
            throws SecurityException, NoSuchMethodException, InterruptedException {
        Class<?> clazz = AopUtils.isAopProxy(obj) ? AopUtils.getTargetClass(obj) : obj.getClass();
        SshdShellCommand annotation = AnnotationUtils.findAnnotation(clazz, SshdShellCommand.class);
        Map<String, CommandSupplier> map = getSupplierMap(sshdShellCommandsMap, annotation);
        loadSshdShellCommandSuppliers(clazz, annotation, map, obj);
    }

    private Map<String, CommandSupplier> getSupplierMap(Map<String, Map<String, CommandSupplier>> sshdShellCommandsMap,
            SshdShellCommand annotation) {
        Map<String, CommandSupplier> map = sshdShellCommandsMap.get(annotation.value());
        if (Objects.isNull(map)) {
            map = new TreeMap<>();
            sshdShellCommandsMap.put(annotation.value(), map);
        }
        return map;
    }

    private void loadSshdShellCommandSuppliers(Class<?> clazz, SshdShellCommand annotation,
            Map<String, CommandSupplier> map, Object obj) throws NoSuchMethodException, SecurityException,
            InterruptedException {
        loadClassLevelCommandSupplier(clazz, annotation, map, obj);
        loadMethodLevelCommandSupplier(clazz, map, obj);
    }

    private void loadClassLevelCommandSupplier(Class<?> clazz, SshdShellCommand annotation,
            Map<String, CommandSupplier> map, Object obj) throws SecurityException, NoSuchMethodException {
        log.debug("Loading class level command supplier for {}", clazz.getName());
        try {
            Method m = clazz.getDeclaredMethod(annotation.value(), String.class);
            log.debug("Adding default command method {}", m.getName());
            map.put(EXECUTE, getMethodSupplier(m, obj));
        } catch (NoSuchMethodException ex) {
            log.debug("Does not contain default command method {}", ex.getMessage());
        }
        map.put(SUMMARY_HELP, arg -> annotation.description());
        map.put(HELP, arg -> annotation.description());
    }

    private CommandSupplier getMethodSupplier(Method m, Object obj) {
        return arg -> {
            try {
                return (String) m.invoke(obj, arg);
            } catch (InvocationTargetException ex) {
                if (ex.getCause() instanceof InterruptedException) {
                    throw (InterruptedException) ex.getCause();
                } else {
                    return getErrorInfo(ex.getCause());
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                return getErrorInfo(ex);
            }
        };
    }

    private String getErrorInfo(Throwable ex) {
        log.error("Error performing method invocation", ex);
        return "Error performing method invocation\r\n" + (log.isDebugEnabled() ? ex
                : "Please check server logs for more information");
    }

    private void loadMethodLevelCommandSupplier(Class<?> clazz, Map<String, CommandSupplier> map, Object obj)
            throws NoSuchMethodException, SecurityException, InterruptedException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(SshdShellCommand.class)) {
                log.debug("{}.#{} is marked with annotation {}", clazz.getName(), m.getName(),
                        SshdShellCommand.class.getName());
                SshdShellCommand command = m.getDeclaredAnnotation(SshdShellCommand.class);
                String help = map.get(HELP).get(null);
                map.put(HELP, arg -> help + "\n\r\t" + command.value() + "\t\t" + command.description());
                map.put(command.value(), getMethodSupplier(m, obj));
            }
        }
    }
}
