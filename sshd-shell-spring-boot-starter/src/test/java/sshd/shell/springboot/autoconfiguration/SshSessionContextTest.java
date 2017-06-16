/*
 * Copyright 2017 anand.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import jline.console.ConsoleReader;
import org.apache.commons.io.output.ByteArrayOutputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.boot.ansi.AnsiColor;

/**
 *
 * @author anand
 */
public class SshSessionContextTest {
    
    @Test
    public void testSshSessionContext() throws IOException {
        assertFalse(SshSessionContext.containsKey("test"));
        SshSessionContext.put("test", "test");
        assertEquals("test", SshSessionContext.get("test"));
        assertTrue(SshSessionContext.containsKey("test"));
        assertEquals("test", SshSessionContext.remove("test"));
        assertFalse(SshSessionContext.containsKey("test"));
        InputStream is = new ByteArrayInputStream("test\r".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SshSessionContext.put(SshSessionContext.CONSOLE_READER, new ConsoleReader(is, os));
        SshSessionContext.put(SshSessionContext.TEXT_COLOR, AnsiColor.DEFAULT);
        SshSessionContext.readInput("hello");
        assertTrue(os.toString(StandardCharsets.UTF_8).contains("hello test"));
        assertFalse(SshSessionContext.isEmpty());
        SshSessionContext.clear();
        assertTrue(SshSessionContext.isEmpty());
    }
}
