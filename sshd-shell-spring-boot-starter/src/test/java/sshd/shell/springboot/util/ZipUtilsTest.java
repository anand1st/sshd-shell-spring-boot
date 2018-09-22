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
package sshd.shell.springboot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 * @author anand
 */
public class ZipUtilsTest {

    @Test
    public void testZipFiles() throws IOException {
        File zipDir = new File("target");
        Files.copy(Paths.get("src/test/resources/banner.txt"), new File(zipDir, "banner.txt").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        Path[] filesToZip = {Paths.get("target/banner.txt")};
        Path result = ZipUtils.zipFiles(zipDir.toPath(), true, filesToZip);
        assertEquals(Paths.get("target/banner.txt.zip"), result);
        assertFalse(new File("target/banner.txt").exists());
    }   
}
