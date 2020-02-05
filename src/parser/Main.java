package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        var namesToFindSet = readNamesList("EnglishNames.txt");
        new ThreadsManager(namesToFindSet,
                "http://norvig.com/big.txt");

    }

    private static Set<String> readNamesList(String textPath) {
        HashSet<String> nameSet = null;
        var filePath = Paths.get(textPath);
        try (Stream<String> lines = Files.lines(filePath)) {
            nameSet = new HashSet<>(lines.flatMap(str -> Arrays.stream(str.split(",")))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nameSet;
    }
}
