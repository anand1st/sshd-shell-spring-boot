package sshd.shell.springboot.autoconfiguration;

import java.util.Map;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;

/**
 *
 * @author anand
 */
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
class SshSessionFactory implements CommandFactory, Factory<Command> {

    private final SshdShellProperties properties;
    private final Map<String, Map<String, CommandSupplier>> commandMap;
    
    @Override
    public Command createCommand(String command) {
        return new SshSessionInstance(properties, commandMap);
    }

    @Override
    public Command create() {
        return createCommand(null);
    }
}
