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
package sshd.shell.springboot.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import sshd.shell.springboot.autoconfiguration.CommandExecutableDetails;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;
import static sshd.shell.springboot.autoconfiguration.SshdShellProperties.AuthType.*;

/**
 *
 * @author anand
 */
@Configuration
@ConditionalOnProperty(name = "sshd.shell.enabled", havingValue = "true")
@lombok.extern.slf4j.Slf4j
class SshdServer {

    @Autowired
    private SshdShellProperties properties;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private Map<String, Map<String, CommandExecutableDetails>> sshdShellCommands;
    @Autowired
    private Environment environment;

    @Bean
    Banner shellBanner() {
        return new ShellBanner(environment);
    }

    @Bean
    PasswordAuthenticator passwordAuthenticator() {
        SshdShellProperties.Shell.Auth props = properties.getShell().getAuth();
        switch (properties.getShell().getAuth().getAuthType()) {
            case SIMPLE:
                return new SimpleSshdPasswordAuthenticator(properties);
            case AUTH_PROVIDER:
                try {
                    AuthenticationProvider authProvider = Objects.isNull(props.getAuthProviderBeanName())
                            ? appContext.getBean(AuthenticationProvider.class)
                            : appContext.getBean(props.getAuthProviderBeanName(), AuthenticationProvider.class);
                    return new AuthProviderSshdPasswordAuthenticator(authProvider);
                } catch (BeansException ex) {
                    throw new IllegalArgumentException("Expected a default or valid AuthenticationProvider bean", ex);
                }
            default:
                throw new IllegalArgumentException("Invalid/Unsupported auth type");
        }
    }

    @Bean
    Factory<Command> sshSessionFactory() {
        return () -> new SshSessionInstance(properties, sshdShellCommands, environment, shellBanner());
    }

    @PostConstruct
    void startServer() throws IOException {
        SshdShellProperties.Shell props = properties.getShell();
        if (Objects.isNull(props.getPassword())) {
            props.setPassword(UUID.randomUUID().toString());
            log.info("********** User password not set. Use following password to login: {} **********",
                    props.getPassword());
        }
        SshServer server = SshServer.setUpDefaultServer();
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(props.getHostKeyFile())));
        server.setPublickeyAuthenticator(Objects.isNull(props.getPublicKeyFile())
                ? RejectAllPublickeyAuthenticator.INSTANCE
                : new SshdAuthorizedKeysAuthenticator(new File(props.getPublicKeyFile())));
        server.setHost(props.getHost());
        server.setPasswordAuthenticator(passwordAuthenticator());
        server.setPort(props.getPort());
        server.setShellFactory(sshSessionFactory());
        server.start();
        props.setPort(server.getPort()); // In case server port is 0, a random port is assigned.
        log.info("SSH server started on port {}", props.getPort());
    }
}
