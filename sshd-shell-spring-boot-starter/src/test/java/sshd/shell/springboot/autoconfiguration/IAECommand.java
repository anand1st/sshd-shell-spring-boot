package sshd.shell.springboot.autoconfiguration;

import org.springframework.stereotype.Component;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "iae", description = "throws IAE")
public class IAECommand {
    
    public void iae(String arg) {
        throw new IllegalArgumentException("iae");
    }
}
