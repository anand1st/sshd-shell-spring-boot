package sshd.shell.springboot.autoconfiguration;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author anand
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ConfigTest.class, properties = {"sshd.shell.enabled=true", "sshd.shell.port=0",
    "sshd.shell.hostKeyFile=target/hostKey.ser", "logging.level.sshd.shell=DEBUG"})
public class SshdShellAutoConfigurationTest {

    @Autowired
    private Map<String, Map<String, CommandSupplier>> sshdCliCommands;
    @Autowired
    private SshdShellProperties properties;

    @Test
    public void testExpectedDataFromMethodCallsAndHelp() throws Exception {
        assertEquals(5, sshdCliCommands.size());
        assertEquals("Supported Commands\n\rdummy\t\tdummy description\n\rexit\t\tExit shell\n\riae\t\tthrows IAE\n\r"
                + "test\t\ttest description", sshdCliCommands.get("help").get(SshdShellAutoConfiguration.EXECUTE)
                        .get(null));
        assertEquals("test description\n\r\trun\t\ttest run\n\r\texecute\t\ttest execute",
                sshdCliCommands.get("test").get(SshdShellAutoConfiguration.HELP).get(null));
        assertEquals("test run successful", sshdCliCommands.get("test").get("run").get("successful"));
        assertEquals("test execute successful", sshdCliCommands.get("test").get("execute").get(null));
        assertEquals("dummy run successful", sshdCliCommands.get("dummy").get("run").get(null));
        assertEquals("dummy description\n\r\trun\t\tdummy run",
                sshdCliCommands.get("dummy").get(SshdShellAutoConfiguration.HELP).get(null));
        assertEquals("Exit shell", sshdCliCommands.get("exit").get(SshdShellAutoConfiguration.HELP).get(null));
    }
    
    @Test
    public void testExitCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("exit\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0mexit\r\n", os.toString());
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testIAECommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("iae\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0miae\r\n", os.toString());
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("xxx\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0mxxx\r\n\u001B[0mUnknown command. Enter 'help' for a list of supported commands\n\r"
                + "\u001B[0m\u001B[0mapp> \u001B[0m", os.toString());
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testUnsupportedSubCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test nonexistent\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0mtest nonexistent\r\n\u001B[0mUnknown sub command 'nonexistent'. Type 'test help' for more "
                + "information\n\r\u001B[0m\u001B[0mapp> \u001B[0m", os.toString());
        channel.disconnect();
        session.disconnect();
    }
    
    @Test
    public void testHelpCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        session.setPassword(properties.getShell().getPassword());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0mtest\r\n\u001B[0mtest description\n\r\trun\t\ttest run\n\r\texecute\t\ttest execute"
                + "\n\r\u001B[0m\u001B[0mapp> \u001B[0m", os.toString());
        channel.disconnect();
        session.disconnect();
    }
}
