package vln.com.map;

import vln.com.battle.FieldOfHonor;
import vln.com.buildings.*;
import vln.com.graphic.Props;
import vln.com.units.*;


import java.util.Random;
import java.util.Scanner;

public class AreaMap {

    public static AreaMap currentMap;
    public static boolean compIsAlive = true;
    private final int height, width;
    public static boolean isBattleStarted = false;
    private Props[][] world;
    private final boolean[][] smokeLayer;
    private int globalTurnCounter = 0;
    private Portal activePortal = null;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public AreaMap(int h, int w, Hero[] heroes, boolean init) {
        currentMap = this;
        height = h;
        width = w;
        world = new Props[h][w];
        smokeLayer = new boolean[h][w];

        if (init) {
            setupHeroes(heroes);
            buildMap(heroes);
            addObstacles();
        }
    }

    private void setupHeroes(Hero[] h) {
        h[0].isPlayer = true;
        h[0].isAvailable = true;
        h[0].heroY = height / 2;
        h[0].heroX = 1;
        h[0].army.put("Lancer", 10);
        h[0].army.put("Archer", 10);

        h[1].heroY = height / 2;
        h[1].heroX = width - 2;
        h[1].army.put("Lancer", 6);
        h[1].army.put("Archer", 3);
    }

    private void buildMap(Hero[] h) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                world[i][j] = (i == height / 2) ? buildCentralRow(j) : buildOtherRows(i, j);
            }
        }
        world[h[0].heroY][h[0].heroX] = h[0];
        world[h[1].heroY][h[1].heroX] = h[1];
    }

    private Props buildCentralRow(int j) {
        if (j == 0) return new PlayerCastle();
        if (j == width - 1) return new BotCastle();
        return new Trail();
    }

    private Props buildOtherRows(int i, int j) {
        if ((j == width * 3 / 8 && i <= height / 2) || (j == width * 5 / 8 && i >= height / 2) || i == height / 5)
            return new Trail();
        return new Field();
    }

    public void applySmoke(int centerY, int centerX) {
        for (int i = centerY - 2; i <= centerY + 2; i++) {
            for (int j = centerX - 2; j <= centerX + 2; j++) {
                if (i >= 0 && i < height && j >= 0 && j < width) {
                    smokeLayer[i][j] = true;
                }
            }
        }
    }

    public void removeSmoke(int centerY, int centerX) {
        for (int i = centerY - 2; i <= centerY + 2; i++) {
            for (int j = centerX - 2; j <= centerX + 2; j++) {
                if (i >= 0 && i < height && j >= 0 && j < width) {
                    smokeLayer[i][j] = false;

                    if (world[i][j] instanceof Smoke) {
                        world[i][j] = getOriginalTerrain(i, j);
                    }
                }
            }
        }
    }

    private void addObstacles() {
        Random rand = new Random();
        int placed = 0;
        while (placed < 10) {
            int y = rand.nextInt(height);
            int x = rand.nextInt(width);
            if (world[y][x] instanceof Field) {
                world[y][x] = new Obstacle();
                placed++;
            }
        }
    }

    public void moveHero(Hero hero, int dx, int dy) {
        int newX = hero.heroX + dx, newY = hero.heroY + dy;
        boolean countedAsMove = false;

        if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
            System.out.println("Don't be tricky, you can't go abroad");
            return;
        }

        Props cell = world[newY][newX];
        Props realCell = (cell instanceof Smoke smoke) ? smoke.originalProp : cell;

        switch (realCell) {
            case Obstacle _ -> System.out.println("Don't step on flowers! 🌸");
            case Portal portal -> {
                handlePortalInteraction(hero, portal);
                countedAsMove = true;
            }
            case Building building -> {
                handleBuilding(hero, newY, newX, building);
                countedAsMove = true;
            }
            case Hero other -> {
                handleCombat(hero, other);
                countedAsMove = true;
            }
            case SmokeUnit smokeUnit -> {
                removeSmoke(smokeUnit.getSmokeCenterY(), smokeUnit.getSmokeCenterX());
                world[newY][newX] = new Field();
                System.out.println("The smoker has been destroyed! The smoke has cleared.");
                updateDisplay();
                countedAsMove = true;
            }
            default -> {
                if (handleMovement(hero, newX, newY, cell)) {
                    countedAsMove = true;
                }
            }
        }

        if (countedAsMove) {
            globalTurnCounter++;
            if (globalTurnCounter % 2 == 0 && activePortal == null && Math.random() <= 0.3) {
                spawnPortal();
            }
            if (activePortal != null) {
                activePortal.turnsSinceSpawn++;
                if (activePortal.turnsSinceSpawn >= 10) {
                    spawnSmokeUnit(activePortal);
                }
            }
        }
    }

    private void spawnPortal() {
        Random rand = new Random();
        int y, x;
        do {
            y = rand.nextInt(height);
            x = rand.nextInt(width);
        } while (!(world[y][x] instanceof Field));

        activePortal = new Portal();
        world[y][x] = activePortal;
        System.out.println("Portal appeared at (" + y + ", " + x + ")!");
        updateDisplay();
    }

    private void handlePortalInteraction(Hero hero, Portal portal) {
        System.out.println("Pay 20 gold to prevent smoke? (Y/N)");
        String answer = new Scanner(System.in).nextLine();

        if (portal == null) return;

        if (answer.equalsIgnoreCase("Y") && hero.gold >= 20) {
            hero.gold -= 20;
            removePortal(portal);
            System.out.println("Portal neutralized!");
            updateDisplay();
        } else {
            spawnSmokeUnit(portal);
        }
    }

    private void removePortal(Portal portal) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world[i][j] == portal) {
                    world[i][j] = new Field();
                }
            }
        }
    }

    private void spawnSmokeUnit(Portal portal) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world[i][j] == portal) {
                    SmokeUnit smoke = new SmokeUnit(this);
                    world[i][j] = smoke;
                    updateDisplay();
                    return;
                }
            }
        }
    }

    public void revealSmokeWithLocator() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world[i][j] instanceof SmokeUnit smokeUnit) {
                    if (i >= smokeUnit.getSmokeCenterY() - 2 && i <= smokeUnit.getSmokeCenterY() + 2 && j >= smokeUnit.getSmokeCenterX() - 2 && j <= smokeUnit.getSmokeCenterX() + 2) {
                        smokeLayer[i][j] = false;
                        System.out.println("Locator has detected a smoker!");
                        updateDisplay();
                        return;
                    }
                }
            }
        }
        System.out.println("Locator did not detect a smoker in the impact zone.");
    }

    public boolean isSmokeAt(int y, int x) {
        return smokeLayer[y][x];
    }

    private void handleBuilding(Hero hero, int y, int x, Building b) {
        if (b.isPlayerTower && hero.isPlayer) {
            manageShop(hero, b);
        } else {
            startBattle(hero, b.aegis, y, x);
        }
    }

    private void manageShop(Hero hero, Building b) {
        Scanner scan = new Scanner(System.in);
        boolean shopping = true;

        while (shopping && hero.gold > 0) {
            System.out.println("Your gold: " + hero.gold);
            b.printBuildings();
            String input = scan.nextLine().toUpperCase();

            if (input.equals("Q")) {
                shopping = false;
                updateDisplay();
            } else {
                b.handleBuildingPurchase(hero, input);
                if (hero.gold > 0) offerUnits(hero, b, scan);
            }
        }
    }

    private void offerUnits(Hero hero, Building b, Scanner scan) {
        System.out.println("Do you want to buy units? (Y/N)");
        if (scan.nextLine().equalsIgnoreCase("Y")) {
            System.out.println("Choose a unit to buy: 1 - Lancer (5), 2 - Archer (8), 3 - Swordsman(10), 4 - Cavalryman (15), 5 - Paladin (25)");
            String choice = scan.nextLine();
            System.out.println("Enter gold amount:");
            int gold = scan.nextInt();
            scan.nextLine();

            if (gold > 0 && gold <= hero.gold) hero.unitPurchase(b, choice, gold);
            else System.out.println("Not enough gold or incorrect amount.");
        }
    }

    private void startBattle(Hero attacker, Hero defender, int y, int x) {
        isBattleStarted = true;
        new FieldOfHonor(attacker, defender).startBattle();
        isBattleStarted = false;
        world = endCastleBattle(attacker, y, x);
    }

    private void handleCombat(Hero attacker, Hero defender) {
        attacker.moves = defender.moves = 0;
        isBattleStarted = true;
        new FieldOfHonor(attacker, defender).startBattle();
        isBattleStarted = false;
        world = endBotBattle(attacker.isPlayer ? attacker : defender, attacker.isPlayer ? defender : attacker);
    }

    private boolean handleMovement(Hero hero, int x, int y, Props terrain) {
        Props real = (terrain instanceof Smoke smoke) ? smoke.originalProp : terrain;
        int cost = (real instanceof Trail) ? 1 : 2;
        if (hero.moves < cost) {
            System.out.println("Not enough movement points");
            return false;
        }
        // списываем ход и двигаем героя
        hero.moves -= cost;
        System.out.println("The field took " + cost + " point" + (cost > 1 ? "s" : ""));
        System.out.println("The hero has " + hero.moves + " moves left");
        int oldX = hero.heroX, oldY = hero.heroY;
        hero.heroX = x;
        hero.heroY = y;
        world[oldY][oldX] = getOriginalTerrain(oldY, oldX);
        world[y][x] = hero;
        updateDisplay();
        return true;
    }

    private Props getOriginalTerrain(int y, int x) {
        if (smokeLayer[y][x]) {
            return new Smoke(getTerrain(y, x)).originalProp;
        }
        return getTerrain(y, x);
    }

    private Props getTerrain(int i, int j) {
        return (i == height / 2) ? buildCentralRow(j) : buildOtherRows(i, j);
    }

    private void clearConsole() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 25; i++) {
            System.out.println();
        }
    }

    public void updateDisplay() {
        clearConsole();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Props cell = world[i][j];
                if (smokeLayer[i][j]) {
                    if (cell instanceof Hero hero && hero.isPlayer) {
                        System.out.print(cell + " ");
                    } else {
                        System.out.print("\u001B[37m▒\u001B[0m ");
                    }
                } else {
                    System.out.print(cell + " ");
                }
            }
            System.out.println();
        }

        if(endGame()) {
            System.exit(0);
        }
    }

    public Props[][] endCastleBattle(Hero playerHero, int newY, int newX) {
        if (compIsAlive) {
            world[newY][newX] = new BotCastle();

            world[playerHero.heroY][playerHero.heroX] = playerHero;
        } else {
            int reward = ((Building) world[newY][newX]).aegis.gold;

            world[newY][newX] = new PlayerCastle();

            if (playerHero.isPlayer) {
                world[playerHero.heroY][playerHero.heroX] = playerHero;
                System.out.println("You received " + reward + " gold from the enemy!");
                playerHero.gold += reward;
            } else {
                world[playerHero.heroY][playerHero.heroX] = new Trail();
            }
        }
        updateDisplay();

        isBattleStarted = false;
        return world;
    }

    public Props[][] endBotBattle(Hero playerHero, Hero computerHero) {
        System.out.println("You received " + computerHero.gold + " gold from the enemy!");
        playerHero.gold += computerHero.gold;
        System.out.println("Your balance is " + playerHero.gold + "!");
        computerHero.gold = 0;
        computerHero.isAvailable = false;

        world[computerHero.heroY][computerHero.heroX] = new Trail();
        world[playerHero.heroY][playerHero.heroX] = playerHero;

        isBattleStarted = false;
        return world;
    }

    public boolean hasAnyMoves(Hero hero) {
        int x = hero.heroX;
        int y = hero.heroY;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int i = 0; i < dx.length; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;

            Props cell = world[ny][nx];
            Props real = (cell instanceof Smoke smoke) ? smoke.originalProp : cell;

            int cost = (real instanceof Trail) ? 1 : 2;

            if (real instanceof Obstacle) continue;

            if (hero.moves >= cost) {
                return true;
            }
        }
        return false;
    }

    public void endRound(Hero[] heroes) {
        for (Hero hero : heroes) {
            if (hero.isAvailable && !isBattleStarted) {
                hero.resetMoves();
            }
        }
    }

    public boolean endGame() {
        if (world[height/2][0] instanceof PlayerCastle &&
                world[height/2][width-1] instanceof PlayerCastle) {
            System.out.println("You won!");
            return true; // Игра завершена победой
        }
        if (world[height/2][0] instanceof BotCastle &&
                world[height/2][width-1] instanceof BotCastle) {
            System.out.println("You lost!");
            return true;
        }
        return false;
    }
}