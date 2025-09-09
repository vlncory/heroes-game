package vln.com;

import vln.com.pattern.AreaMap;
import vln.com.leaderboard.Leaderboard;
import vln.com.units.Hero;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Play {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String userName = scanner.nextLine().trim();
        System.out.println("Welcome, " + userName + "!");

        while (true) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> startGame(userName, scanner);
                case "2" -> loadGame(scanner, userName);
                case "3" -> editMap(scanner);
                case "4" -> displayLeaderboard();
                case "5" -> {
                    System.out.println("Exiting the game...");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }

    private static void displayLeaderboard() {
        try {
            Leaderboard leaderboard = Leaderboard.loadFromFile();
            var topRecords = leaderboard.getTopRecords(5);
            System.out.println("\n=== Leaderboard (Top 5) ===");
            if (topRecords.isEmpty()) {
                System.out.println("No records yet.");
            } else {
                for (int i = 0; i < topRecords.size(); i++) {
                    Leaderboard.Record record = topRecords.get(i);
                    System.out.printf("%d. %s - %d points (Map: %s)%n",
                            i + 1, record.username(), record.score(), record.mapName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading leaderboard: " + e.getMessage());
        }
    }

    private static void printMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. New game");
        System.out.println("2. Load game");
        System.out.println("3. Map editor");
        System.out.println("4. Records");
        System.out.println("5. Quit");
        System.out.print("Choose an option: ");
    }

    private static void startGame(String userName, Scanner scanner) {
        Hero[] heroes = initializeHeroes();

        AreaMap map = chooseMap(heroes, scanner, userName);
        if (map == null) {
            return;
        }

        map.updateDisplay();
        System.out.println("\n=== GAME ===");
        System.out.println("WASD - movement, P - save, Q - exit");

        gameLoop(map, heroes, scanner);
    }

    private static AreaMap chooseMap(Hero[] heroes, Scanner scanner, String userName) {
        while (true) {
            System.out.println("\n=== Choose Map ===");
            System.out.println("1. Standard map");
            System.out.println("2. Custom maps");
            System.out.println("3. Back to main menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    return new AreaMap(10, 10, heroes, true, userName);
                }
                case "2" -> {
                    return chooseCustomMap(heroes, scanner, userName);
                }
                case "3" -> {
                    return null;
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }

    private static AreaMap chooseCustomMap(Hero[] heroes, Scanner scanner, String userName) {
        File mapsDir = new File("src/main/resources/maps/");
        if (!mapsDir.exists() || !mapsDir.isDirectory()) {
            System.out.println("No custom maps available.");
            return null;
        }

        File[] mapFiles = mapsDir.listFiles((_, name) -> name.endsWith(".map"));

        if (mapFiles == null || mapFiles.length == 0) {
            System.out.println("No custom maps found.");
            return null;
        }

        System.out.println("\nAvailable custom maps:");
        for (int i = 0; i < mapFiles.length; i++) {
            String name = mapFiles[i].getName().replace(".map", "");
            System.out.println((i + 1) + ". " + name);
        }
        System.out.println((mapFiles.length + 1) + ". Back");
        System.out.print("Choose a map: ");

        String input = scanner.nextLine();
        try {
            int index = Integer.parseInt(input) - 1;
            if (index == mapFiles.length) {
                return null;
            }
            if (index >= 0 && index < mapFiles.length) {
                String mapName = mapFiles[index].getName().replace(".map", "");
                return new AreaMap(mapName, heroes, userName);
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IOException e) {
            System.out.println("Error loading map: " + e.getMessage());
        }
        return null;
    }

    private static Hero[] initializeHeroes() {
        Hero[] heroes = new Hero[2];
        for (int i = 0; i < heroes.length; i++) {
            heroes[i] = new Hero(100);
        }
        heroes[0].setSymbol("\u001B[34m" + "♡" + "\u001B[0m");
        heroes[1].setSymbol("\u001B[31m" + "♡" + "\u001B[0m");
        return heroes;
    }

    private static void gameLoop(AreaMap map, Hero[] heroes, Scanner scanner) {
        while (true) {
            while (heroes[0].moves > 0 && map.hasAnyMoves(heroes[0])) {
                String input = scanner.nextLine().trim().toLowerCase();

                switch (input) {
                    case "w" -> map.moveHero(heroes[0], 0, -1);
                    case "s" -> map.moveHero(heroes[0], 0, 1);
                    case "a" -> map.moveHero(heroes[0], -1, 0);
                    case "d" -> map.moveHero(heroes[0], 1, 0);
                    case "p" -> {
                        if (!AreaMap.isBattleStarted) {
                            try {
                                map.saveGame("manual_save_" + System.currentTimeMillis(), heroes);
                                System.out.println("Game saved!");
                            } catch (IOException e) {
                                System.out.println("Save failed: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Cannot save during battle!");
                        }
                    }
                    case "q" -> {
                        System.out.println("Exiting game...");
                        return;
                    }
                    default -> System.out.println("Invalid input");
                }
            }

            while (heroes[1].moves > 0 && AreaMap.compIsAlive && map.hasAnyMoves(heroes[1])) {
                System.out.println("Computer's turn...");
                map.moveHero(heroes[1], -1, 0);
            }

            map.endRound(heroes);
            System.out.println("Moves restored");
            map.updateDisplay();
        }
    }

    private static void loadGame(Scanner scanner, String userName) {
        File savesDir = new File("src/main/saves/" + userName);
        if (!savesDir.exists() || !savesDir.isDirectory()) {
            System.out.println("No saves available.");
            return;
        }

        File[] saveFiles = savesDir.listFiles((_, name) -> name.endsWith(".sav"));

        if (saveFiles == null || saveFiles.length == 0) {
            System.out.println("No saves found.");
            return;
        }

        System.out.println("\nAvailable saves:");
        for (int i = 0; i < saveFiles.length; i++) {
            String name = saveFiles[i].getName().replace(".sav", "");
            System.out.println((i + 1) + ". " + name);
        }
        System.out.println((saveFiles.length + 1) + ". Back");
        System.out.print("Choose a save: ");

        String input = scanner.nextLine();
        try {
            int index = Integer.parseInt(input) - 1;
            if (index == saveFiles.length) {
                return;
            }
            if (index >= 0 && index < saveFiles.length) {
                String saveName = saveFiles[index].getName().replace(".sav", "");
                Hero[] heroes = initializeHeroes();
                AreaMap map = AreaMap.loadGame(saveName, userName, heroes);
                map.updateDisplay();
                System.out.println("\n=== GAME LOADED ===");
                gameLoop(map, heroes, scanner);
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading save: " + e.getMessage());
        }
    }

    private static void editMap(Scanner scanner) {
        File mapsDir = new File("src/main/resources/maps/");
        File[] mapFiles = mapsDir.exists() && mapsDir.isDirectory() ?
                mapsDir.listFiles((_, name) -> name.endsWith(".map")) : new File[0];

        while (true) {
            System.out.println("\n=== Map Editor ===");
            if (mapFiles == null || mapFiles.length == 0) {
                System.out.println("No custom maps available.");
            } else {
                System.out.println("Available custom maps:");
                for (int i = 0; i < mapFiles.length; i++) {
                    String name = mapFiles[i].getName().replace(".map", "");
                    System.out.println((i + 1) + ". " + name);
                }
            }
            assert mapFiles != null;
            System.out.println((mapFiles.length + 1) + ". Create new map");
            System.out.println((mapFiles.length + 2) + ". Back to main menu");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index == mapFiles.length) {
                    // Create new map
                    AreaMap editorMap = new AreaMap(10, 10, null, false);
                    editorMap.editMode(scanner);
                    // Update file list after editing
                    mapFiles = mapsDir.listFiles((_, name) -> name.endsWith(".map"));
                } else if (index == mapFiles.length + 1) {
                    // Back to main menu
                    return;
                } else if (index >= 0 && index < mapFiles.length) {
                    // Selected existing map
                    String mapName = mapFiles[index].getName().replace(".map", "");
                    if (handleMapOptions(mapName, scanner)) {
                        // Update file list after deletion
                        mapFiles = mapsDir.listFiles((_, name) -> name.endsWith(".map"));
                    }
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    private static boolean handleMapOptions(String mapName, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Map: " + mapName + " ===");
            System.out.println("1. Edit");
            System.out.println("2. Delete");
            System.out.println("3. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    try {
                        AreaMap editorMap = new AreaMap(mapName, null);
                        editorMap.editMode(scanner);
                        return false; // No need to update list after edit
                    } catch (IOException e) {
                        System.out.println("Error loading map: " + e.getMessage());
                    }
                }
                case "2" -> {
                    File mapFile = new File("src/main/resources/maps/" + mapName + ".map");
                    if (mapFile.delete()) {
                        System.out.println("Map " + mapName + " deleted successfully.");
                        return true; // Update list after deletion
                    } else {
                        System.out.println("Error deleting map.");
                    }
                }
                case "3" -> {
                    return false;
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }
}