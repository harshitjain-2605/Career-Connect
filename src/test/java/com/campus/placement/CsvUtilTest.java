package com.campus.placement;

import com.campus.placement.util.CsvUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvUtil}.
 */
class CsvUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void writeAndReadCsv_preservesCommasAndQuotes() throws Exception {
        Path csvFile = tempDir.resolve("report.csv");
        String[] headers = {"RollNo", "Name", "Skills"};
        List<String[]> rows = List.of(
                new String[]{"102203001", "Aarav, Sharma", "Java, \"SQL\", JavaFX"},
                new String[]{"102203002", "Diya Patel", "Python"}
        );

        CsvUtil.writeCsv(csvFile, headers, rows);
        List<String[]> readBack = CsvUtil.readCsv(csvFile);

        assertEquals(3, readBack.size());
        assertArrayEquals(headers, readBack.get(0));
        assertArrayEquals(rows.get(0), readBack.get(1));
        assertArrayEquals(rows.get(1), readBack.get(2));
    }
}
