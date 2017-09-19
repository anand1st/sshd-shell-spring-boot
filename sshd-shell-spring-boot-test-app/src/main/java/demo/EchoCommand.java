package demo;

import java.io.IOException;
import org.springframework.stereotype.Component;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;
import sshd.shell.springboot.console.ConsoleIO;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "echo", description = "Echo by users. Type 'echo' for supported subcommands")
public class EchoCommand {
    
    @SshdShellCommand(value = "bob", description = "Bob's echo. Usage: echo bob <arg>")
    public String bobSays(String arg) throws IOException {
        String name = ConsoleIO.readInput("What's your name?");
        SshSessionContext.put("name", name);
        return "bob echoes " + arg + " and your name is " + name;
    }
    
    @SshdShellCommand(value = "alice", description = "Alice's echo. Usage: echo alice <arg>")
    public String aliceSays(String arg) {
        String str = "";
        if (SshSessionContext.containsKey("name")) {
            str = ", Name " + SshSessionContext.get("name") + " exists";
        }
        return "alice says " + arg + str;
    }
    
    @SshdShellCommand(value = "admin", description = "Admin's echo. Usage: echo admin <arg>", roles = "ADMIN")
    public String adminSays(String arg) {
        return "admin says " + arg;
    }
}
