package de.tuberlin.tfdacmacs.crypto.benchmark.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;

public class CSVPrinter {

    public static void writeCSV(String fileName, String line, boolean append) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileOutputStream(new java.io.File(fileName), append));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        writer.println(line);
        writer.close();
    }
}
