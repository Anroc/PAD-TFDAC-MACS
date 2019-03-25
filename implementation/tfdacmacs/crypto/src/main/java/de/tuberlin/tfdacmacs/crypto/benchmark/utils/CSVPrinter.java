package de.tuberlin.tfdacmacs.crypto.benchmark.utils;

import java.io.*;

public class CSVPrinter {

    public static void writeCSV(String fileName, String line, boolean append) {
        PrintWriter writer;
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            writer = new PrintWriter(new FileOutputStream(file, append));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        writer.println(line);
        writer.close();
    }
}
