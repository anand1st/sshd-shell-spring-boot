/*
 * Copyright 2018 anand.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sshd.shell.springboot.autoconfiguration;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author anand
 */
@SpringBootTest(classes = ConfigTest.class, properties = {
    "sshd.filetransfer.enabled=true",
    "sshd.filesystem.base.dir=target/sftp"
})
public class SshdSftpEnabledTest extends AbstractSshSupport {

    @DirtiesContext
    @Test
    public void testSftp() throws Exception {
        Session session = openSession(props.getShell().getUsername(), props.getShell().getPassword());
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        sftp.disconnect();
        session.disconnect();
        assertTrue(new File("target/sftp/admin").exists());
    }

    @DirtiesContext
    @Test
    public void testSftp2() throws Exception {
        Session session = openSession(props.getShell().getUsername(), props.getShell().getPassword());
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        sftp.disconnect();
        session.disconnect();
        assertTrue(new File("target/sftp/admin").exists());
    }

    @Test
    public void testHeapDumpWifhSftpEnabled() {
        sshCallShell((InputStream is, OutputStream os) -> {
            write(os, "heapDump live true");
            verifyResponse(is, "Resource can be downloaded with SFTP/SCP at banner.txt");
        });
    }
}
