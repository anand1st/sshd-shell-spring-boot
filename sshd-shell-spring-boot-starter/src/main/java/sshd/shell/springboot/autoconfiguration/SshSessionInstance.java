package sshd.shell.springboot.autoconfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import jline.console.ConsoleReader;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
class SshSessionInstance implements Command, Runnable {

    private static final Properties ANSI_PROPERTIES = new Properties();
    private static final String SUPPORTED_COMMANDS_MESSAGE = "Enter '" + SshdShellAutoConfiguration.HELP
            + "' for a list of supported commands";
    private static final String UNSUPPORTED_COMMANDS_MESSAGE = "Unknown command. " + SUPPORTED_COMMANDS_MESSAGE;

    static {
        InputStream inputStream = SshSessionInstance.class.getResourceAsStream("/ansi-colors.properties");
        try {
            ANSI_PROPERTIES.load(inputStream);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load ansi-colors.properties in classpath", ex);
        }
    }
    private final SshdShellProperties properties;
    private final Map<String, Map<String, CommandSupplier>> commandMap;
    private InputStream is;
    private OutputStream os;
    private ExitCallback callback;
    private Thread sshThread;
    private PrintWriter writer;

    SshSessionInstance(SshdShellProperties properties, Map<String, Map<String, CommandSupplier>> commandMap) {
        this.properties = properties;
        this.commandMap = commandMap;
    }

    @Override
    public void start(Environment env) throws IOException {
        sshThread = new Thread(this, "sshd-cli");
        sshThread.start();
    }

    @Override
    public void run() {
        try (ConsoleReader reader = new ConsoleReader(is, os)) {
            reader.setPrompt(ANSI_PROPERTIES.getProperty(properties.getShell().getPrompt().getColor())
                    + properties.getShell().getPrompt().getTitle() + "> " + ANSI_PROPERTIES.getProperty("default"));
            writer = new PrintWriter(reader.getOutput());
            writeResponse(SUPPORTED_COMMANDS_MESSAGE);
            String line;
            while ((line = reader.readLine()) != null) {
                handleUserInput(line.trim());
            }
        } catch (IOException ex) {
            log.error("Error with console reader", ex);
        } catch (InterruptedException ex) {
            log.info(ex.getMessage());
        } finally {
            callback.onExit(0);
        }
    }

    private void writeResponse(String response) {
        writer.println(ANSI_PROPERTIES.getProperty(properties.getShell().getText().getColor()) + response);
        writer.write(ConsoleReader.RESET_LINE);
        writer.write(ANSI_PROPERTIES.getProperty("default"));
        writer.flush();
    }

    private void handleUserInput(String command) throws InterruptedException {
        String[] part = command.split(" ", 3);
        Map<String, CommandSupplier> supplier = commandMap.get(part[0]);
        if (Objects.isNull(supplier)) {
            writeResponse(UNSUPPORTED_COMMANDS_MESSAGE);
        } else if (part.length < 2) {
            writeResponse((supplier.containsKey(SshdShellAutoConfiguration.EXECUTE)
                    ? supplier.get(SshdShellAutoConfiguration.EXECUTE)
                    : supplier.get(SshdShellAutoConfiguration.HELP)).get(null));
        } else if (supplier.containsKey(part[1])) {
            writeResponse(supplier.get(part[1]).get(part.length == 2 ? null : part[2]));
        } else {
            writeResponse("Unknown sub command '" + part[1] + "'. Type '" + part[0] + " help' for more information");
        }
    }

    @Override
    public void destroy() throws Exception {
        sshThread.interrupt();
    }

    @Override
    public void setErrorStream(OutputStream errOS) {
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        callback = ec;
    }

    @Override
    public void setInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        this.os = os;
    }
}
