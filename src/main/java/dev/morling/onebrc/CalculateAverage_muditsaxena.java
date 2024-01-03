/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.onebrc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class CalculateAverage_muditsaxena {

    private static final String FILE = "./measurements.txt";

    private static record Measurement(String station, double value) {
        private Measurement(String[] parts) {
            this(parts[0], Double.parseDouble(parts[1]));
        }
    }

    private static record ResultRow(double min, double mean, double max) {
        public String toString() {
            return round(min) + "/" + round(mean) + "/" + round(max);
        }

        private double round(double value) {
            return Math.round(value * 10.0) / 10.0;
        }
    };

    private static class MeasurementAggregator {
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        private double sum;
        private long count;
    }

    static class TaskRunner<V> implements Callable<V> {

        List<String> inputList;
        String input;

        TaskRunner(List<String> taskList) {
            this.inputList = taskList;
        }

        TaskRunner(String input) {
            this.input = input;
        }

        // @Override
        // public V call() throws Exception {
        // for (String inputTask : inputList) {
        // Measurement measurement = new Measurement(inputTask.split(";"));
        // MeasurementAggregator measurementAggregator = map.getOrDefault(measurement.station(), new MeasurementAggregator());
        // measurementAggregator.count += 1;
        // measurementAggregator.sum += measurement.value;
        // measurementAggregator.max = Math.max(measurementAggregator.max, measurement.value);
        // measurementAggregator.min = Math.min(measurementAggregator.min, measurement.value);
        // map.put(measurement.station(), measurementAggregator);
        // }
        // inputList = null;
        // return null;
        // }

        @Override
        public V call() throws Exception {
            Measurement measurement = new Measurement(this.input.split(";"));
            MeasurementAggregator measurementAggregator = map.getOrDefault(measurement.station(), new MeasurementAggregator());
            measurementAggregator.count += 1;
            measurementAggregator.sum += measurement.value;
            measurementAggregator.max = Math.max(measurementAggregator.max, measurement.value);
            measurementAggregator.min = Math.min(measurementAggregator.min, measurement.value);
            map.put(measurement.station(), measurementAggregator);
            return null;
        }
    }

    static ConcurrentMap<String, MeasurementAggregator> map = new ConcurrentHashMap<>();
    static final int TASK_LIST_CAPACITY = 100;

    public static void main(String[] args) throws IOException {
        // ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
        ExecutorService virtualThreadExecutors = Executors.newVirtualThreadPerTaskExecutor();

        List<String> taskList = new ArrayList<>(TASK_LIST_CAPACITY);
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        int index = 0;
        // int arrayCapacity = 500000; // 1000000
        // CompletableFuture<Void>[] taskFutures = new CompletableFuture[arrayCapacity];

//        Files.lines(Path.of(FILE))
//                .parallel()
//                .map(line -> tasks.add(CompletableFuture.runAsync(new FutureTask<>(new TaskRunner<>(line)), virtualThreadExecutors)));

        // try (BufferedReader br = Files.newBufferedReader(Paths.get(FILE))) {
        // while (br.readLine() != null) {
        // String line = br.readLine();
        // // Try creating virtual thread per line? How will that perform
        // // tasks.add(CompletableFuture.runAsync(new FutureTask<>(new TaskRunner<>(line)), virtualThreadExecutors));
        //
        // taskList.add(line);
        //
        // if (taskList.size() >= TASK_LIST_CAPACITY) {
        // tasks.add(CompletableFuture.runAsync(new FutureTask<>(new TaskRunner<>(taskList)), virtualThreadExecutors));
        // taskList = null;
        // taskList = new ArrayList<>(TASK_LIST_CAPACITY);
        // }
        // }
        // }

        // CompletableFuture<Void>[] taskFutures = tasks.stream()
        // .map(task -> CompletableFuture.runAsync(task, executorService))
        // .toArray(CompletableFuture[]::new);

        for (CompletableFuture<Void> task : tasks) {
            if (task != null) {
                task.join();
            }
        }

        // for (CompletableFuture<Void> task : taskFutures) {
        // if (task != null) {
        // task.join();
        // }
        // }

        // CompletableFuture.allOf(taskFutures).join();

        Map<String, ResultRow> resultRowMap = new TreeMap<>();
        map.forEach((key, value) -> resultRowMap.put(key, new ResultRow(value.min, value.sum / value.count, value.max)));
        System.out.println(resultRowMap);

        // for (Map.Entry<String, MeasurementAggregator> entry : map.entrySet()) {
        // System.out.print();
        // }
        // Map<String, ResultRow> measurements = map.entrySet().stream().parallel().collect(Collectors.toMap(entry -> entry.getKey(),
        // entry -> new ResultRow(entry.getValue().min, entry.getValue().sum / entry.getValue().count, entry.getValue().max), TreeMap::new));
        //
        // System.out.println(measurements);
    }
}
