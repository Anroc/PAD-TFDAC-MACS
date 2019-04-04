package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.benchmark.utils.CSVPrinter;
import lombok.Data;

import java.time.temporal.ChronoUnit;

@Data
public class BenchmarkCombinedResult {

    private final BenchmarkResult rsaResult;
    private final BenchmarkResult abeResult;
    private final BenchmarkResult abe2FaResult;

    public void csvPrintHeaders(String fileName) {
        CSVPrinter.writeCSV(fileName, "x,numberOfAttributes,rsaAvgTime,rsaMedTime,rsaAvgCTSize,rsaNumberOfFileKeys,abeAvgTime,abeMedTime,abeAvgCTSize,abeNumberOfFileKeys,abe2FaAvgTime,abe2FaMedTime,abe2FaAvgCTSize,abe2FaNumberOfFileKeys", false);
    }

    public void csvPrintStatistics(String fn, int x, int numerOfAttributes) {
        CSVPrinter.writeCSV(fn,
                String.format("%d,%d,%f,%f,%f,%d,%f,%f,%f,%d,%f,%f,%f,%d",
                        x,
                        numerOfAttributes,
                        rsaResult.averageTime(),
                        rsaResult.medianTime(),
                        rsaResult.averageSize(),
                        rsaResult.getNumberOfFileKeys(),
                        abeResult.averageTime(),
                        abeResult.medianTime(),
                        abeResult.averageSize(),
                        abeResult.getNumberOfFileKeys(),
                        abe2FaResult.averageTime(),
                        abe2FaResult.medianTime(),
                        abe2FaResult.averageSize(),
                        abe2FaResult.getNumberOfFileKeys()),
                true);
    }

    public void setUnit(ChronoUnit unit) {
        rsaResult.setUnit(unit);
        abeResult.setUnit(unit);
        abe2FaResult.setUnit(unit);
    }

    public void prittyPrintStatistics(int numUsers) {
        rsaResult.prittyPrintStatistics(numUsers);
        abeResult.prittyPrintStatistics(numUsers);
        abe2FaResult.prittyPrintStatistics(numUsers);
    }
}
