package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.benchmark.utils.CSVPrinter;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Data
public class BenchmarkResult {

    private List<RunTimeResult> runs = new ArrayList<>();
    private ChronoUnit unit = ChronoUnit.NANOS;

    public BenchmarkResult addRun(RunTimeResult runTime) {
        runs.add(runTime);
        return this;
    }

    public long getNumberOfFileKeys() {
        return getRuns().get(0).getNumberOfFileKeys();
    }

    public double averageTime() {
        return time().average().getAsDouble();
    }

    public double averageSize() {
        return size().average().getAsDouble();
    }

    public double medianSize() {
        return median(size());
    }

    public double medianTime() {
        return median(time());
    }

    private double median(DoubleStream doubleStream) {
        List<Double> runs = doubleStream.boxed().collect(Collectors.toList());
        int size = runs.size();
        if (size % 2 == 0) {
            return runs.get(size / 2);
        } else {
            return (runs.get(size / 2) + runs.get((size + 1) / 2)) / 2.0;
        }
    }

    public DoubleStream time() {
        return runs.stream().mapToDouble(l -> convert(l.getTime()));
    }
    public DoubleStream size() {
        return runs.stream().mapToDouble(l -> l.getCipherTextLength());
    }

    private double convert(double time) {
        return time / unit.getDuration().getNano();
    }

    public void prittyPrintResult(int numUsers) {
        String stringRuns = StringUtils.collectionToDelimitedString(time().boxed().collect(Collectors.toList()), ", ");
        System.out.println("runs: " + stringRuns);
        prittyPrintStatistics(numUsers);
    }

    public void prittyPrintStatistics(int numUsers) {
        System.out.println(
                String.format("users: %d, avg: %f, med: %f", numUsers, averageTime(), medianTime())
        );
    }

    public void csvPrintHeaders(String fileName) {
        CSVPrinter.writeCSV(fileName, "numUsers,avg,med", false);
    }

    public void csvPrintStatistics(String fn, int numUsers) {
        CSVPrinter.writeCSV(fn, String.format("%d,%f,%f", numUsers, averageTime(), medianTime()), true);
    }
}
