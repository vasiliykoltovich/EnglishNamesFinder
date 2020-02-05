package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class ThreadsManager {
    static final String PILL = "poisonPill";
    private static final int PIECE_LIMIT = 3000;
    private final Set<String> namesToSearchSet;
    private final BlockingQueue<Map<String, List<NamePosition>>> foundedNamesContainer = new LinkedBlockingDeque<>(500);
    private String readUrl;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private HttpURLConnection urlConnection;
    private Thread aggregatorThread;

    public ThreadsManager(Set<String> namesToFindSet, String textToReadUrl) {
        this.namesToSearchSet = namesToFindSet;
        this.readUrl = textToReadUrl;
        initiate();
    }

    private void initiate() {
        try {
            startSearching();
            stopSearching();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            stopExecution();
        }
    }

    private void stopExecution() {
        try {
            aggregatorThread.join();
            urlConnection.disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void startReadingText() throws IOException {
        InputStream inputStream = urlConnection.getInputStream();
        String line;
        long currentLineOffset = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<String> linesCapacity = new ArrayList<>(PIECE_LIMIT);

        while ((line = reader.readLine()) != null) {
            linesCapacity.add(line);

            if (linesCapacity.size() == PIECE_LIMIT) {
                startSearchingForName(linesCapacity, currentLineOffset);
                currentLineOffset += PIECE_LIMIT;
                linesCapacity = new ArrayList<>(PIECE_LIMIT);
            }
        }

        if (linesCapacity.size() > 0) {
            startSearchingForName(linesCapacity, currentLineOffset);
        }
        reader.close();
    }

    private void stopSearching() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        } finally {
            HashMap<String, List<NamePosition>> dummy = new HashMap<>();
            dummy.put(PILL, null);
            foundedNamesContainer.add(dummy);
        }

    }

    private void startSearchingForName(ArrayList<String> linesCapacity, long currentLineOffset) {
        executor.execute(new NameMatcher(foundedNamesContainer, linesCapacity, namesToSearchSet, currentLineOffset));
    }

    private void startSearching() throws IOException {
        var source = new URL(readUrl);
        urlConnection = (HttpURLConnection) source.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(50000);
        urlConnection.setReadTimeout(30000);
        urlConnection.connect();
        aggregatorThread = new Thread(new Aggregator(foundedNamesContainer));
        aggregatorThread.start();
        startReadingText();
    }

}
