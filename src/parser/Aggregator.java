package parser;

import java.util.*;
import java.util.concurrent.BlockingQueue;

class Aggregator implements Runnable {

    private final BlockingQueue<Map<String, List<NamePosition>>> foundedNamesContainer;
    private Map<String, Set<NamePosition>> nameUnits;
    private String outputFile;

    public Aggregator(BlockingQueue<Map<String, List<NamePosition>>> foundedNamesContainer) {
        this.foundedNamesContainer = foundedNamesContainer;
        nameUnits = new HashMap<>();
    }

    @Override
    public void run() {
        collectFoundedMatchings();
        printResultsToConsole(getResults());
    }

    private void collectFoundedMatchings() {
        boolean hasProducers = true;
        while (hasProducers) {
            try {
                Map<String, List<NamePosition>> nameUnit = foundedNamesContainer.take();
                if (nameUnit.containsKey(ThreadsManager.PILL)) {
                    hasProducers = false;
                } else {
                    for (Map.Entry<String, List<NamePosition>> singleNameUnit : nameUnit.entrySet()) {
                        Set<NamePosition> listOfPositionsForNameSet = nameUnits.getOrDefault(singleNameUnit.getKey(), new HashSet<>());
                        listOfPositionsForNameSet.addAll(singleNameUnit.getValue());
                        nameUnits.put(singleNameUnit.getKey(), listOfPositionsForNameSet);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    private String getResults() {
        var output = new StringBuilder();
        for (Map.Entry<String, Set<NamePosition>> nameUnit : nameUnits.entrySet()) {
            var key = nameUnit.getKey();
            var value = nameUnit.getValue();
            output.append(key).append(" --> ").append("[");
            for (NamePosition item : value) {
                output.append(item).append(",");
            }
            output.replace(output.length() - 1, output.length(), "");
            output.append("]").append('\n');
        }

        return output.toString();
    }
    private void printResultsToConsole(String results) {
        System.out.println(results);
    }

}
