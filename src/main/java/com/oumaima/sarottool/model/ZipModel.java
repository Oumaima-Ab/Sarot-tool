// 📁 ZipModel.java
// Language: Java
// Purpose: Provide static methods to zip a folder into a .zip file, and to unzip a .zip file into a target directory.
// Usage: Used by a desktop encryption tool to compress folders before encryption, and extract them after decryption.

package com.oumaima.sarottool.model;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipModel {

    // Compresses a directory into a .zip file.
    public static File zipFolder(File source) throws IOException {
        File zipFile = new File(source.getParent(), source.getName() + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Path sourcePath = source.toPath();
            if (source.isDirectory()) {
                try {
                    Files.walk(sourcePath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e); // propagate error
                            }
                        });
                } catch (UncheckedIOException e) {
                    throw e.getCause(); // rethrow as IOException
                }
            } else {
                ZipEntry zipEntry = new ZipEntry(source.getName());
                zos.putNextEntry(zipEntry);
                Files.copy(sourcePath, zos);
                zos.closeEntry();
            }
        }
        return zipFile;
    }

    // Extracts a .zip file into the given target directory.
    public static void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) targetDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
