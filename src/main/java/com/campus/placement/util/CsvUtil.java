package com.campus.placement.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV export and import utility built with Java character streams
 * ({@link BufferedWriter}/{@link FileWriter} and {@link BufferedReader}/{@link FileReader}).
 */
// [Unit 3: Character streams, file read/write (CSV export/import)]
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Writes headers and rows to a CSV file, properly escaping quotes and commas.
     *
     * @param targetFile destination path
     * @param headers    column headers
     * @param rows       list of row string arrays
     * @throws IOException if writing fails
     */
    public static void writeCsv(Path targetFile, String[] headers, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile.toFile(), StandardCharsets.UTF_8))) {
            if (headers != null && headers.length > 0) {
                writer.write(formatRow(headers));
                writer.newLine();
            }
            for (String[] row : rows) {
                writer.write(formatRow(row));
                writer.newLine();
            }
        }
    }

    /**
     * Reads all rows from a CSV file using {@link BufferedReader}.
     *
     * @param sourceFile source CSV file
     * @return list of parsed columns per line
     * @throws IOException if reading fails
     */
    public static List<String[]> readCsv(Path sourceFile) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile.toFile(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(parseLine(line));
                }
            }
        }
        return rows;
    }

    private static String formatRow(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
