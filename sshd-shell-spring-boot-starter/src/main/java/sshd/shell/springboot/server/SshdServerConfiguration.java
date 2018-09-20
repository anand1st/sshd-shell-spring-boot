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
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import sshd.shell.springboot.autoconfiguration.Constants;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties.Shell;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties.Shell.Auth;
import sshd.shell.springboot.console.TerminalProcessor;

/**
 *
 * @author anand
 */
@Configuration
@ConditionalOnProperty(name = "sshd.shell.enabled", havingValue = "true")
@lombok.extern.slf4j.Slf4j
class SshdServerConfiguration {

    @Autowired
    private SshdShellProperties properties;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private Environment environment;
    @Autowired
    private TerminalProcessor terminalProcessor;
    @Qualifier(Constants.SHELL_BANNER)
    @Autowired
    private Banner shellBanner;

    @Bean
    SshServer sshServer() {
        Shell props = properties.getShell();
        if (Objects.isNull(props.getPassword())) {
            props.setPassword(UUID.randomUUID().toString());
            log.info("********** User password not set. Use following password to login: {} **********",
                    props.getPassword());
        }
        SshServer server = buildServer(props);
        configureSecurityPolicies(server, props);
        if (properties.getFiletransfer().isEnabled()) {
            configureServerForSshAndFileTransfer(server);
        } else {
            configureServerForSshOnly(server);
        }
        Optional<String> baseDir = properties.getFiletransfer().isEnabled()
                ? Optional.of(properties.getFilesystem().getBase().getDir())
                : Optional.empty();
        server.setShellFactory(() -> sshSessionInstance(baseDir));
        return server;
    }

    private SshServer buildServer(Shell props) {
        SshServer server = SshServer.setUpDefaultServer();
        server.setHost(props.getHost());
        server.setPort(props.getPort());
        return server;
    }

    private void configureSecurityPolicies(SshServer server, Shell props) {
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(props.getHostKeyFile())));
        server.setPublickeyAuthenticator(Objects.isNull(props.getPublicKeyFile())
                ? RejectAllPublickeyAuthenticator.INSTANCE
                : new SshdAuthorizedKeysAuthenticator(new File(props.getPublicKeyFile())));
        server.setPasswordAuthenticator(passwordAuthenticator(props));
    }

    private PasswordAuthenticator passwordAuthenticator(Shell props) {
        Auth authProps = props.getAuth();
        switch (authProps.getAuthType()) {
            case SIMPLE:
                return new SimpleSshdPasswordAuthenticator(props);
            case AUTH_PROVIDER:
                return authProviderAuthenticator(authProps);
            default:
                throw new IllegalArgumentException("Invalid/Unsupported auth type");
        }
    }

    private PasswordAuthenticator authProviderAuthenticator(Auth authProps) throws IllegalArgumentException {
        try {
            AuthenticationProvider authProvider = Objects.isNull(authProps.getAuthProviderBeanName())
                    ? appContext.getBean(AuthenticationProvider.class)
                    : appContext.getBean(authProps.getAuthProviderBeanName(), AuthenticationProvider.class);
            return new AuthProviderSshdPasswordAuthenticator(authProvider);
        } catch (BeansException ex) {
            throw new IllegalArgumentException("Expected a default or valid AuthenticationProvider bean", ex);
        }
    }

    private void configureServerForSshAndFileTransfer(SshServer server) {
        server.setCommandFactory(sshAndScpCommandFactory());
        server.setFileSystemFactory(new SshdNativeFileSystemFactory(properties.getFilesystem().getBase().getDir()));
        server.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystemFactory.Builder()
                .withShutdownOnExit(true).build()));
    }

    private CommandFactory sshAndScpCommandFactory() {
        ScpCommandFactory scpCommandFactory = new ScpCommandFactory();
        scpCommandFactory.setDelegateCommandFactory(
                sshCommandFactory(Optional.of(properties.getFilesystem().getBase().getDir())));
        return scpCommandFactory;
    }

    private CommandFactory sshCommandFactory(Optional<String> baseDir) {
        return command -> sshSessionInstance(baseDir);
    }

    private SshSessionInstance sshSessionInstance(Optional<String> baseDir) {
        return new SshSessionInstance(terminalProcessor, baseDir,
                (clazz, printStream) -> shellBanner.printBanner(environment, clazz, printStream));
    }

    private void configureServerForSshOnly(SshServer server) {
        server.setCommandFactory(sshCommandFactory(Optional.<String>empty()));
    }

    @PostConstruct
    void startServer() throws IOException {
        SshServer server = sshServer();
        server.start();
        properties.getShell().setPort(server.getPort()); // In case server port is 0, a random port is assigned.
        log.info("SSH server started on port {}", properties.getShell().getPort());
    }

    @PreDestroy
    void stopServer() throws IOException {
        sshServer().stop();
        log.info("SSH server stopped");
    }
}
