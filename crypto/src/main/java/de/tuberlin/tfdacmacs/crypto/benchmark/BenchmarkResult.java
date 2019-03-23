package de.tuberlin.tfdacmacs.crypto.benchmark;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Data
public class BenchmarkResult {

    private List<Long> runs = new ArrayList<>();
    private ChronoUnit unit = ChronoUnit.NANOS;

    public BenchmarkResult addRun(long runTime) {
        runs.add(runTime);
        return this;
    }

    public double average() {
        return asDoubleStream().average().getAsDouble();
    }

    public double median() {
        List<Double> runs = asDoubleStream().boxed().collect(Collectors.toList());
        int size = runs.size();
        if(size % 2 == 0) {
            return runs.get(size / 2);
        } else {
            return (runs.get(size / 2) + runs.get((size + 1) / 2)) / 2.0;
        }
    }

    public DoubleStream asDoubleStream() {
        return runs.stream().mapToDouble(l -> convert(l));
    }

    private List<Long> copyRuns() {
        return new ArrayList<>(this.runs);
    }

    private double convert(double time) {
        return time / unit.getDuration().getNano();
    }

    public void prittyPrintResult(int numUsers) {
        String stringRuns = StringUtils.collectionToDelimitedString(asDoubleStream().boxed().collect(Collectors.toList()), ", ");
        System.out.println("runs: " + stringRuns);
        prittyPrintStatistics(numUsers);
    }

    public void prittyPrintStatistics(int numUsers) {
        System.out.println(
                String.format("users: %d, avg: %f, med: %f", numUsers, average(), median())
        );
    }

    public void csvPrintHeaders(String fileName) {
        writeCSV(fileName, "numUsers,avg,med", false);
    }

    public void csvPrintStatistics(String fn, int numUsers) {
        writeCSV(fn, String.format("%d,%f,%f", numUsers, average(), median()), true);
    }

    private void writeCSV(String fileName, String line, boolean append) {
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
