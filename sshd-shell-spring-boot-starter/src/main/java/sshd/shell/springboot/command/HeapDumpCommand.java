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
package sshd.shell.springboot.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.autoconfiguration.SshdShellCommand;

/**
 *
 * @author anand
 */
@Component
@ConditionalOnClass(HeapDumpWebEndpoint.class)
@ConditionalOnProperty(name = "management.endpoint.env.enabled", havingValue = "true", matchIfMissing = true)
@SshdShellCommand(value = "heapDump", description = "Heap dump command")
@lombok.extern.slf4j.Slf4j
public final class HeapDumpCommand {

    @Autowired
    private HeapDumpWebEndpoint heapDumpEndpoint;

    @SshdShellCommand(value = "live", description = "Get heapdump with live flag")
    public String withLive(String arg) throws IOException {
        if (StringUtils.isEmpty(arg)) {
            return "Usage: heapDump live <true|false>";
        }
        Resource heapDumpResource = heapDumpEndpoint.heapDump(Boolean.valueOf(arg)).getBody();
        try {
            Path path = sessionUserPathContainingHeapDumpFile(heapDumpResource);
            return "Resource can be downloaded with SFTP/SCP at " + path.getFileName().toString();
        } catch (IllegalStateException ex) {
            log.warn(ex.getMessage());
            return "Resource can be found at " + heapDumpResource.getFile().getAbsolutePath();
        }
    }

    private Path sessionUserPathContainingHeapDumpFile(Resource heapDumpResource) throws IOException {
        Path userDirFilePath = Paths.get(sessionUserDir().getPath(), heapDumpResource.getFilename());
        Path heapDumpFilePath = Paths.get(heapDumpResource.getURI());
        return Files.move(heapDumpFilePath, userDirFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private File sessionUserDir() throws IOException {
        File sessionUserDir = SshSessionContext.getUserDir();
        if (!sessionUserDir.exists()) {
            Files.createDirectories(sessionUserDir.toPath());
        }
        return sessionUserDir;
    }
}
