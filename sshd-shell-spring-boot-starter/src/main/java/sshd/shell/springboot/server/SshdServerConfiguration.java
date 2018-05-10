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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.root.RootedFileSystem;
import org.apache.sshd.common.file.root.RootedFileSystemProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import sshd.shell.springboot.autoconfiguration.SshdShellProperties;
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
    @Qualifier("__shellBanner")
    @Autowired
    private Banner shellBanner;
    @Value("${sshd.filesystem.base.dir:#{systemProperties['user.home']}}")
    private String filesystemBaseDir;
    @Value("${sshd.filetransfer.enabled:false}")
    private boolean isFileTransferEnabled;

    @Bean
    SshServer sshServer() {
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
        server.setShellFactory(() -> sshSessionInstance());
        if (isFileTransferEnabled) {
            server.setCommandFactory(sshAndScpCommandFactory());
            server.setFileSystemFactory(fileSystemFactory());
            server.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(
                    new SftpSubsystemFactory.Builder()
                            .withShutdownOnExit(true)
                            .withExecutorService(Executors.newFixedThreadPool(4))
                            .build()));
        } else {
            server.setCommandFactory(sshCommandFactory());
        }
        return server;
    }

    private PasswordAuthenticator passwordAuthenticator() {
        SshdShellProperties.Shell.Auth props = properties.getShell().getAuth();
        switch (props.getAuthType()) {
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

    private SshSessionInstance sshSessionInstance() {
        return new SshSessionInstance(environment, shellBanner, terminalProcessor);
    }

    private CommandFactory sshAndScpCommandFactory() {
        ScpCommandFactory scpCommandFactory = new ScpCommandFactory();
        scpCommandFactory.setDelegateCommandFactory(sshCommandFactory());
        return scpCommandFactory;
    }

    private CommandFactory sshCommandFactory() {
        return (command) -> sshSessionInstance();
    }

    private FileSystemFactory fileSystemFactory() {
        RootedFileSystemProvider fileSystemProvider = new RootedFileSystemProvider();
        return new NativeFileSystemFactory() {
            @Override
            public FileSystem createFileSystem(Session session) throws IOException {
                Path sessionUserDir = Paths.get(filesystemBaseDir, session.getUsername());
                if (Files.exists(sessionUserDir)) {
                    if (!Files.isDirectory(sessionUserDir)) {
                        throw new NotDirectoryException(sessionUserDir.toString());
                    }
                } else {
                    log.info("Session user directory created: {}", Files.createDirectories(sessionUserDir));
                }
                return new RootedFileSystem(fileSystemProvider, sessionUserDir, null);
            }
        };
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
