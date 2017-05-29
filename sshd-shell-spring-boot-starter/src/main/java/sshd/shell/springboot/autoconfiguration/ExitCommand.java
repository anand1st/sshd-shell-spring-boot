package sshd.shell.springboot.autoconfiguration;

import org.springframework.stereotype.Component;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "exit", description = "Exit shell")
public class ExitCommand {
    
    final String exit(String arg) throws InterruptedException {
        throw new InterruptedException("Exiting shell");
    }
}
