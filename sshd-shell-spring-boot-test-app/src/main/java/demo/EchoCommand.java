package demo;

import java.io.IOException;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo help' for supported subcommands")
public class EchoCommand {
    
    @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
    public String bobSays(String arg) throws IOException {
        return "bob echoes " + arg + " and hello " + SshSessionContext.readInput("What's your name?");
    }
    
    @SshdShellCommand(value = "alice", description = "Alice's echo. Usage: echo alice <arg>")
    public String aliceSays(String arg) {
        return "alice says " + arg;
    }
}
