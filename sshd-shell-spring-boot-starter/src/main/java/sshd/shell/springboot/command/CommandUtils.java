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
import org.springframework.core.io.Resource;
import sshd.shell.springboot.autoconfiguration.SshSessionContext;
import sshd.shell.springboot.util.ZipUtils;

/**
 *
 * @author anand
 */
@lombok.extern.slf4j.Slf4j
public enum CommandUtils {
    ;

    public static Path sessionUserPathContainingZippedResource(Resource resource) throws IOException {
        Path heapDumpFilePath = Paths.get(resource.getURI());
        return ZipUtils.zipFiles(sessionUserDir(), true, heapDumpFilePath);
    }

    private static Path sessionUserDir() throws IOException {
        File sessionUserDir = SshSessionContext.getUserDir();
        if (!sessionUserDir.exists()) {
            Files.createDirectories(sessionUserDir.toPath());
        }
        return sessionUserDir.toPath();
    }

    public static String process(JsonProcessor processor) {
        try {
            return processor.process();
        } catch (IOException | IllegalArgumentException ex) {
            log.warn("Invalid json", ex);
            return "Expected valid json as argument";
        }
    }

    @FunctionalInterface
    public static interface JsonProcessor {

        String process() throws IOException;
    }
}
