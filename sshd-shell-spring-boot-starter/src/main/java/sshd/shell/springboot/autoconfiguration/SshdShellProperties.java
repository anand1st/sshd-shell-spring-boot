package sshd.shell.springboot.autoconfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author anand
 */
@ConfigurationProperties(prefix = "sshd")
@lombok.Data
public class SshdShellProperties {

    private final Shell shell = new Shell();

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

        @lombok.Data
        public static class Prompt {

            private String color = "default";
            private String title = "app";
        }

        @lombok.Data
        public static class Text {

            private String color = "default";
        }
    }
}
