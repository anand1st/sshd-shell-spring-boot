package sshd.shell.springboot.autoconfiguration;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
    "sshd.shell.hostKeyFile=target/hostKey.ser", "sshd.shell.publicKeyFile=src/test/resources/id_rsa.pub",
    "logging.level.sshd.shell=DEBUG"})
public class SshdShellAutoConfiguration2Test {
    
    @Autowired
    private SshdShellProperties properties;
    
    @Test
    public void testTestCommand() throws JSchException, InterruptedException {
        JSch jsch = new JSch();
        Session session = jsch.getSession("admin", "localhost", properties.getShell().getPort());
        jsch.addIdentity("src/test/resources/id_rsa");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(new CharSequenceInputStream("test run bob\r", StandardCharsets.UTF_8));
        OutputStream os = new ByteArrayOutputStream();
        channel.setOutputStream(os);
        channel.connect();
        Thread.sleep(1000);
        assertEquals("\u001B[0mEnter 'help' for a list of supported commands\n\r\u001B[0m\u001B[0mapp> "
                + "\u001B[0mtest run bob\r\n\u001B[0mtest run bob\n\r\u001B[0m\u001B[0mapp> \u001B[0m", os.toString());
        channel.disconnect();
        session.disconnect();
    }
}
