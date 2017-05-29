package sshd.shell.springboot.autoconfiguration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author anand
 */
@Component
@SshdShellCommand(value = "dummy", description = "dummy description")
class DummyCommand {
    
    @Transactional
    @SshdShellCommand(value = "run", description = "dummy run")
    String run(String arg) {
        return "dummy run successful";
    }
}
