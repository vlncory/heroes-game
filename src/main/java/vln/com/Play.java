package vln.com;

import vln.com.map.AreaMap;
import vln.com.units.Hero;

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
                case "2" -> System.out.println("\nMap editor in development...");
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
        final int HEIGHT = 10;
        final int WIDTH = 10;
        Hero[] heroes = initializeHeroes();
        AreaMap map = new AreaMap(HEIGHT, WIDTH, heroes, true);

        map.updateDisplay();
        System.out.println("\n=== GAME ===");
        System.out.println("WASD - movement & Q - exit");

        gameLoop(map, heroes, scanner);
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
}