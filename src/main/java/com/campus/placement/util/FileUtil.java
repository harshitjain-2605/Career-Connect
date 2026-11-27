package com.campus.placement.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Byte-stream file utility for copying student resume files into the
 * application-managed {@code resumes/} directory.
 */
// [Unit 3: Byte streams (FileInputStream / FileOutputStream)]
public final class FileUtil {

    private static final int BUFFER_SIZE = 8192;

    private FileUtil() {
    }

    /**
     * Copies a resume file into {@code resumesDir} using buffered byte streams.
     *
     * @param sourceFile selected resume file
     * @param resumesDir target directory (e.g., {@code resumes})
     * @param rollNo     student roll number used to prefix the stored filename
     * @return relative path to the copied resume file
     * @throws IOException if copying fails
     */
    public static String copyResume(File sourceFile, String resumesDir, String rollNo) throws IOException {
        Path dirPath = Paths.get(resumesDir);
        Files.createDirectories(dirPath);

        String safeName = sourceFile.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String targetFileName = rollNo + "_" + System.currentTimeMillis() + "_" + safeName;
        Path targetPath = dirPath.resolve(targetFileName);

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(targetPath.toFile()))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return targetPath.toString();
    }
}
