package sshd.shell.springboot.autoconfiguration;

import org.springframework.stereotype.Component;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "test", description = "test description")
public class TestCommand {
    
    @SshdShellCommand(value = "run", description = "test run")
    final String run(String arg) {
        return "test run " + arg;
    }
    
    @SshdShellCommand(value = "execute", description = "test execute")
    final String execute(String arg) {
        return "test execute successful";
    }
}
