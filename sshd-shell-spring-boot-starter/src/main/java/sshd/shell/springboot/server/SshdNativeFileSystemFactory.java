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
package sshd.shell.springboot.server;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.root.RootedFileSystem;
import org.apache.sshd.common.file.root.RootedFileSystemProvider;
import org.apache.sshd.common.session.SessionContext;

/**
 *
 * @author anand
 */
@lombok.RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
class SshdNativeFileSystemFactory extends NativeFileSystemFactory {

    private final String baseDir;

    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException {
        Path sessionUserDir = Paths.get(baseDir, session.getUsername());
        processForSessionUserDirectory(sessionUserDir);
        return new RootedFileSystem(new RootedFileSystemProvider(), sessionUserDir, null);
    }

    private void processForSessionUserDirectory(Path sessionUserDir) throws IOException {
        if (Files.exists(sessionUserDir)) {
            validateSessionUserDir(sessionUserDir);
        } else {
            log.info("Session user directory created: {}", Files.createDirectories(sessionUserDir));
        }
    }

    private void validateSessionUserDir(Path sessionUserDir) throws NotDirectoryException {
        if (!Files.isDirectory(sessionUserDir)) {
            throw new NotDirectoryException(sessionUserDir.toString());
        }
    }
}
