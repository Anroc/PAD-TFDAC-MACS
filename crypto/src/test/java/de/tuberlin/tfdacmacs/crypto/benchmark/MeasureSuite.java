package de.tuberlin.tfdacmacs.crypto.benchmark;

import java.util.function.Supplier;

public class MeasureSuite {

    public static final int NUM_RUNS = 50;

    /**
     * We are using a supplier here so that the computation does not get optimized by the JVM.
     * @param processor
     */
    public static void measure(double[][] runs, int run, Supplier<?> processor) {
        for (int i = 0; i < NUM_RUNS; i++) {
            runs[run][i] = (double) measureRun(processor) / 1000000.0;
            System.out.println(run + ":" + runs[run][i]);
        }
    }

    private static long measureRun(Supplier<?> processor) {
        long start = System.nanoTime();
        processor.get();
        return System.nanoTime() - start;
    }
}
