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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.util.Assert;

/**
 *
 * @author anand
 */
public enum ZipUtils {
    ;
    
    public static Path zipFiles(Path dirToStoreZipFile, boolean isDeleteOriginalFiles, Path... filesToZip) throws
            IOException {
        validate(dirToStoreZipFile, filesToZip);
        String zipFileName = getZipFileName(filesToZip);
        Path zipFilePath = dirToStoreZipFile.resolve(zipFileName);
        runZipOperation(zipFilePath, filesToZip);
        cleanUp(isDeleteOriginalFiles, filesToZip);
        return zipFilePath;
    }

    private static void validate(Path dirToStoreZipFile, Path[] filesToZip) {
        File dir = dirToStoreZipFile.toFile();
        Assert.isTrue(dir.exists() && dir.isDirectory(), "File is does not exist or it's not a directory");
        Assert.notEmpty(filesToZip, "Require at least one file for zip");
    }

    private static String getZipFileName(Path[] filesToZip) {
        return (filesToZip.length == 1
                ? filesToZip[0].getFileName().toString()
                : "Files " + LocalDateTime.now().toString()) + ".zip";
    }

    private static void runZipOperation(Path zipFilePath, Path[] filesToZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (Path fileToZip : filesToZip) {
                addZipEntriesIntoZipFile(fileToZip, zipOut);
            }
        }
    }

    private static void addZipEntriesIntoZipFile(Path fileToZip, final ZipOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileToZip.toFile())) {
            ZipEntry zipEntry = new ZipEntry(fileToZip.toFile().getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

    private static void cleanUp(boolean isDeleteOriginalFiles, Path[] filesToZip) throws IOException {
        if (isDeleteOriginalFiles) {
            for (Path fileToZip : filesToZip) {
                Files.deleteIfExists(fileToZip);
            }
        }
    }
}
