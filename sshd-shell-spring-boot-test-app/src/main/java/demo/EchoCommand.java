package demo;

import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
public class EchoCommand {
    
    @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
    public String bobSays(String arg) {
        return "bob says " + arg;
    }
    
    @SshdShellCommand(value = "alice", description = "Alice's echo. Usage: echo alice <arg>")
    public String aliceSays(String arg) {
        return "alice says " + arg;
    }
}
