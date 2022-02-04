package client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryHandler {
    public static void writeHistory(String msg, String login) {
        try (BufferedWriter write = new BufferedWriter(new FileWriter("client/history_" + login + ".txt", true))) {
            write.append(msg).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readHistory(String login) {
        List<String> h = null;
        if (Files.exists(Paths.get("client/history_" + login + ".txt"))) {
            try {
                h = new ArrayList<>(Files.readAllLines(Paths.get("client/history_" + login + ".txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert h != null;
            return h.subList(h.size() - Math.min(h.size(), 100), h.size());
        } else {
            h = Collections.singletonList("No saved history found");
            return h;
        }
    }
}
