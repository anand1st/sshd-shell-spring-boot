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
import javax.annotation.PostConstruct;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.ImageBanner;
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
@ComponentScan
@ConditionalOnProperty(name = "sshd.shell.enabled", havingValue = "true")
@EnableConfigurationProperties(SshdShellProperties.class)
@lombok.extern.slf4j.Slf4j
public class SshdShellAutoConfiguration {

    public static final String HELP = "help";
    public static final String EXECUTE = "__execute";
    private static final String[] SUPPORTED_IMAGES = {"gif", "jpg", "png"};
    private final String summaryHelp = "__summaryHelp";

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
        server.setShellFactory(new SshSessionFactory(properties, sshdCliCommands(), environment, shellBanner()));
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
            Resource imageBanner = resourceLoader.getResource(location);
            if (imageBanner.exists()) {
                banners.addBanner(new ImageBanner(imageBanner));
            }
        } else {
            for (String ext : SUPPORTED_IMAGES) {
                Resource resource = resourceLoader.getResource("banner." + ext);
                if (resource.exists()) {
                    banners.addBanner(new ImageBanner(resource));
                    break;
                }
            }
        }
        Resource textBanner = resourceLoader.getResource(environment.getProperty("banner.location", "banner.txt"));
        if (textBanner.exists()) {
            banners.addBanner(new ShellResourceBanner(textBanner));
        }
        return banners;
    }

    @Bean
    Map<String, Map<String, CommandSupplier>> sshdCliCommands() throws NoSuchMethodException,
            InterruptedException {
        Map<String, Map<String, CommandSupplier>> sshdCliCommands = new TreeMap<>();
        for (Map.Entry<String, Object> entry : appContext.getBeansWithAnnotation(SshdShellCommand.class).entrySet()) {
            loadSshdCliSuppliers(sshdCliCommands, entry.getValue());
        }
        StringBuilder sb = new StringBuilder("Supported Commands");
        for (Map.Entry<String, Map<String, CommandSupplier>> entry : sshdCliCommands.entrySet()) {
            sb.append("\n\r").append(entry.getKey()).append("\t\t")
                    .append(entry.getValue().remove(summaryHelp).get(null));
        }
        sshdCliCommands.put(HELP, Collections.singletonMap(EXECUTE, arg -> sb.toString()));
        return sshdCliCommands;
    }

    private void loadSshdCliSuppliers(Map<String, Map<String, CommandSupplier>> sshdCliCommandMap, Object obj)
            throws SecurityException, NoSuchMethodException, InterruptedException {
        Class<?> clazz = AopUtils.isAopProxy(obj) ? AopUtils.getTargetClass(obj) : obj.getClass();
        SshdShellCommand annotation = AnnotationUtils.findAnnotation(clazz, SshdShellCommand.class);
        Map<String, CommandSupplier> map = getSupplierMap(sshdCliCommandMap, annotation);
        loadSshdCliSuppliers(clazz, annotation, map, obj);
    }

    private Map<String, CommandSupplier> getSupplierMap(Map<String, Map<String, CommandSupplier>> sshdCliCommandMap,
            SshdShellCommand annotation) {
        Map<String, CommandSupplier> map = sshdCliCommandMap.get(annotation.value());
        if (Objects.isNull(map)) {
            map = new TreeMap<>();
            sshdCliCommandMap.put(annotation.value(), map);
        }
        return map;
    }

    private void loadSshdCliSuppliers(Class<?> clazz, SshdShellCommand annotation, Map<String, CommandSupplier> map,
            Object obj) throws NoSuchMethodException, SecurityException, InterruptedException {
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
        map.put(summaryHelp, arg -> annotation.description());
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
                    throw new IllegalArgumentException("Error with command implementation", ex);
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                log.error("Error performing method invocation", ex);
                return "Error performing method invocation." + (log.isDebugEnabled() ? "\n" + ex
                        : " Please check server logs for more information");
            }
        };
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
