package parser;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

class NameMatcher implements Runnable {
    private final List<String> linesCapacity;
    private final Set<String> nameSet;
    private final BlockingQueue<Map<String, List<NamePosition>>> foundedNamesContainer;
    private long firstLineOffset;
    private final String startBounder = "\\b(";
    private final String endBounder = ")\\b";

    public NameMatcher(BlockingQueue<Map<String, List<NamePosition>>> foundedNamesContainer, List<String> linesCapacity, Set<String> nameSet,
                       long firstLineOffset) {
        this.foundedNamesContainer = foundedNamesContainer;
        this.linesCapacity = linesCapacity;
        this.nameSet = nameSet;
        this.firstLineOffset = firstLineOffset;
    }

    @Override
    public void run() {
        Map<String, List<NamePosition>> foundedNameMatchings = new HashMap<>();
        long firstCharOffset = 0;

        for (var line : linesCapacity) {
            for (var textToFind : nameSet) {
                var matcher = Pattern.compile(startBounder + textToFind + endBounder, Pattern.CASE_INSENSITIVE).matcher(line);
                while (matcher.find()) {
                    var currentMatchCharOffset = (firstCharOffset + matcher.start(1));
                    var listOfPositionsForName = foundedNameMatchings.getOrDefault(textToFind, new ArrayList<>());
                    listOfPositionsForName.add(new NamePosition(firstLineOffset, currentMatchCharOffset));
                    foundedNameMatchings.put(textToFind, listOfPositionsForName);
                }
            }

            firstCharOffset += line.length();
        }
        if (foundedNameMatchings.size() > 0) {
            foundedNamesContainer.add(foundedNameMatchings);
        }
    }

}
