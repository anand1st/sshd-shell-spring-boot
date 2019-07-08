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

import org.springframework.boot.context.properties.ConfigurationProperties;
import sshd.shell.springboot.console.ColorType;

/**
 *
 * @author anand
 */
@ConfigurationProperties(prefix = "sshd")
@lombok.Data
public class SshdShellProperties {

    private final FileTransfer filetransfer = new FileTransfer();
    private final Filesystem filesystem = new Filesystem();
    private final Shell shell = new Shell();

    @lombok.Data
    public static class FileTransfer {

        private boolean enabled = false;
    }

    @lombok.Data
    public static class Filesystem {

        private final Base base = new Base();

        @lombok.Data
        public static class Base {

            private String dir = System.getProperty("user.home");
        }
    }

    @lombok.Data
    public static class Shell {

        private int port = 8022;
        private boolean enabled = false;
        private String username = "admin";
        private String password;
        private String publicKeyFile;
        private String host = "127.0.0.1";
        private String hostKeyFile = "hostKey.ser";
        private final Prompt prompt = new Prompt();
        private final Text text = new Text();
        private final Auth auth = new Auth();

        @lombok.Data
        public static class Prompt {

            private ColorType color = ColorType.BLACK;
            private String title = "app";
        }

        @lombok.Data
        public static class Text {

            private ColorType color = ColorType.BLACK;
            private ColorType highlightColor = ColorType.YELLOW;
            private String usageInfoFormat = "%n%-35s%s";
        }

        @lombok.Data
        public static class Auth {

            public enum AuthType {
                SIMPLE,
                AUTH_PROVIDER
            }

            private AuthType authType = AuthType.SIMPLE;
            private String authProviderBeanName;
        }
    }
}
