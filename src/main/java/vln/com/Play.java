package vln.com;

import vln.com.pattern.AreaMap;
import vln.com.units.Hero;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Play {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String userName = scanner.nextLine();
        System.out.println("Welcome, " + userName + "!");

        while (true) {
            printMainMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> startGame(userName, scanner);
                case "2" -> editMap(scanner);
                case "3" -> System.out.println("\nThe leaderboard is in development...");
                case "4" -> {
                    System.out.println("Exiting the game...");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Play");
        System.out.println("2. Map editor");
        System.out.println("3. Records");
        System.out.println("4. Quit");
        System.out.print("Choose an option: ");
    }

    private static void startGame(String userName, Scanner scanner) {
        Hero[] heroes = initializeHeroes();

        AreaMap map = chooseMap(heroes, scanner);
        if (map == null) {
            return; // Вернулись в меню
        }

        map.updateDisplay();
        System.out.println("\n=== GAME ===");
        System.out.println("WASD - movement & Q - exit");

        gameLoop(map, heroes, scanner);
    }

    private static AreaMap chooseMap(Hero[] heroes, Scanner scanner) {
        while (true) {
            System.out.println("\n=== Choose Map ===");
            System.out.println("1. Standard map");
            System.out.println("2. Custom maps");
            System.out.println("3. Back to main menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> {
                    return new AreaMap(10, 10, heroes, true);
                }
                case "2" -> {
                    return chooseCustomMap(heroes, scanner);
                }
                case "3" -> {
                    return null;
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }

    private static AreaMap chooseCustomMap(Hero[] heroes, Scanner scanner) {
        File mapsDir = new File("src/main/java/vln/com/maps/");
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
                return new AreaMap(mapName, heroes);
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

    private static void editMap(Scanner scanner) {
        File mapsDir = new File("src/main/java/vln/com/maps/");
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
                    // Создать новую карту
                    AreaMap editorMap = new AreaMap(10, 10, null, false);
                    editorMap.editMode(scanner);
                    // Обновляем список файлов после редактирования
                    mapFiles = mapsDir.exists() && mapsDir.isDirectory() ?
                            mapsDir.listFiles((_, name) -> name.endsWith(".map")) : new File[0];
                } else if (index == mapFiles.length + 1) {
                    // Вернуться в главное меню
                    return;
                } else if (index >= 0 && index < mapFiles.length) {
                    // Выбрана существующая карта
                    String mapName = mapFiles[index].getName().replace(".map", "");
                    if (handleMapOptions(mapName, scanner)) {
                        // Обновляем список файлов после удаления
                        mapFiles = mapsDir.exists() && mapsDir.isDirectory() ?
                                mapsDir.listFiles((_, name) -> name.endsWith(".map")) : new File[0];
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
                        AreaMap editorMap = new AreaMap(mapName, null); // Загружаем карту без героев
                        editorMap.editMode(scanner);
                        return true; // Возвращаем true, чтобы обновить список файлов
                    } catch (IOException e) {
                        System.out.println("Error loading map: " + e.getMessage());
                    }
                }
                case "2" -> {
                    File mapFile = new File("src/main/java/vln/com/maps/" + mapName + ".map");
                    if (mapFile.delete()) {
                        System.out.println("Map " + mapName + " deleted successfully.");
                        return true; // Обновляем список файлов
                    } else {
                        System.out.println("Error deleting map.");
                    }
                }
                case "3" -> {
                    return false; // Не обновляем список
                }
                default -> System.out.println("Invalid input! Try again");
            }
        }
    }
}