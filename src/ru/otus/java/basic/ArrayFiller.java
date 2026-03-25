package ru.otus.java.basic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ArrayFiller {
    private static final int ARRAY_SIZE = 100_000_000;
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) {
        System.out.println("+--Однопоточное заполнение--+");
        fillSingleThread();

        System.out.println("\n+--Многопоточное заполнение (4 потока)--+");
        fillMultiThread();
    }

    private static void fillSingleThread() {
        double[] array = new double[ARRAY_SIZE];
        long startTime = System.nanoTime();

        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = computeValue(i);
        }

        long endTime = System.nanoTime();
        double elapsedSec = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Время выполнения: %.3f сек\n", elapsedSec);
    }

    private static void fillMultiThread() {
        double[] array = new double[ARRAY_SIZE];
        long startTime = System.nanoTime();

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            int chunkSize = ARRAY_SIZE / THREAD_COUNT;
            int remainder = ARRAY_SIZE % THREAD_COUNT;

            for (int t = 0; t < THREAD_COUNT; t++) {
                int start = t * chunkSize;
                int end = (t == THREAD_COUNT - 1) ? ARRAY_SIZE : (t + 1) * chunkSize;
                executor.submit(new ArrayFillerTask(array, start, end));
            }

            // Завершаем приём новых задач и ждём завершения всех запущенных
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Ожидание потоков было прервано");
                executor.shutdownNow();
            }
        }

        long endTime = System.nanoTime();
        double elapsedSec = (endTime - startTime) / 1_000_000_000.0;

        System.out.printf("Время выполнения: %.3f сек\n", elapsedSec);
    }


    private static double computeValue(int i) {
        return 1.14 * Math.cos(i) * Math.sin(i * 0.2) * Math.cos(i / 1.2);
    }


    private record ArrayFillerTask(double[] array, int start, int end) implements Runnable {

        @Override
            public void run() {
                for (int i = start; i < end; i++) {
                    array[i] = computeValue(i);
                }
            }
        }
}
