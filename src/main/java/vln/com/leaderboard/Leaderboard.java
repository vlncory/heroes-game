package vln.com.leaderboard;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard implements Serializable { // Добавлено для стабильной сериализации
    private final List<Record> records;

    public record Record(String username, int score, String mapName) implements Serializable {
    }

    public Leaderboard() {
        records = new ArrayList<>();
    }

    public void addRecord(String username, int score, String mapName) {
        records.removeIf(record -> record.username().equals(username));
        records.add(new Record(username, score, mapName));
        records.sort(Comparator.comparingInt(Record::score).reversed());
    }

    public List<Record> getTopRecords(int limit) {
        return records.stream().limit(limit).toList();
    }

    public void saveToFile() throws IOException {
        String basePath = System.getProperty("user.dir");
        File dir = new File(basePath, "src/main/resources/leaderboard");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create leaderboard directory: " + dir.getAbsolutePath());
        }
        File file = new File(dir, "leaderboard.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public static Leaderboard loadFromFile() throws IOException, ClassNotFoundException {
        String basePath = System.getProperty("user.dir");
        File file = new File(basePath, "src/main/resources/leaderboard/leaderboard.dat");
        if (!file.exists()) {
            return new Leaderboard();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Leaderboard) ois.readObject();
        }
    }
}