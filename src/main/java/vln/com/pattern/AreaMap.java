package vln.com.pattern;

import vln.com.battle.FieldOfHonor;
import vln.com.buildings.*;
import vln.com.graphic.Props;
import vln.com.leaderboard.Leaderboard;
import vln.com.units.*;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class AreaMap implements Serializable {

    public static AreaMap currentMap; // transient, as it's static
    public static boolean compIsAlive = true;
    private final int height, width;
    public static boolean isBattleStarted = false;
    private Props[][] world;
    private final Props[][] background;
    private final boolean[][] smokeLayer;
    private int globalTurnCounter = 0;
    private Portal activePortal = null;
    private final String username; // New field for username
    private String currentMapName; // New field for current map name in editor

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public AreaMap(int h, int w, Hero[] heroes, boolean init, String username) {
        currentMap = this;
        height = h;
        width = w;
        this.username = username;
        world = new Props[h][w];
        background = new Props[h][w];
        smokeLayer = new boolean[h][w];

        if (init) {
            setupHeroes(heroes); // Set coordinates first
            buildMap(); // Fill map without heroes
            world[heroes[0].heroY][heroes[0].heroX] = heroes[0]; // Place heroes
            world[heroes[1].heroY][heroes[1].heroX] = heroes[1];
            addObstacles(); // Add obstacles after heroes
        }
    }

    public AreaMap(String mapFile, Hero[] heroes, String username) throws IOException {
        this(10, 10, heroes, false, username);
        this.currentMapName = mapFile; // Set current map name for editing
        loadFromFile(mapFile);
        if (heroes != null) {
            setupHeroes(heroes);
            world[heroes[0].heroY][heroes[0].heroX] = heroes[0];
            world[heroes[1].heroY][heroes[1].heroX] = heroes[1];
            placeCastles();
        } else {
            placeCastles(); // Only castles for editor
        }
    }

    // Overload for editor without username
    public AreaMap(int h, int w, Hero[] heroes, boolean init) {
        this(h, w, heroes, init, null);
    }

    public AreaMap(String mapFile, Hero[] heroes) throws IOException {
        this(mapFile, heroes, null);
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

    private void buildMap() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Props terrain = (i == height / 2) ? buildCentralRow(j) : buildOtherRows(i, j);
                world[i][j] = terrain;
                background[i][j] = terrain;
            }
        }
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

    private void placeCastles() {
        world[height / 2][0] = new PlayerCastle();
        world[height / 2][width - 1] = new BotCastle();
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
                        world[i][j] = background[i][j];
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
            if (world[y][x] instanceof Field &&
                    !(y == height / 2 && x == 1) && // Protect player position
                    !(y == height / 2 && x == width - 2)) { // Protect bot position
                world[y][x] = new Obstacle();
                background[y][x] = new Obstacle();
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
                world[newY][newX] = background[newY][newX];
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
        hero.moves -= cost;
        System.out.println("The field took " + cost + " point" + (cost > 1 ? "s" : ""));
        System.out.println("The hero has " + hero.moves + " moves left");
        int oldX = hero.heroX, oldY = hero.heroY;
        hero.heroX = x;
        hero.heroY = y;
        world[oldY][oldX] = background[oldY][oldX];
        world[y][x] = hero;
        updateDisplay();
        return true;
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

        if (endGame()) {
            if (world[height/2][0] instanceof PlayerCastle &&
                    world[height/2][width-1] instanceof PlayerCastle) {
                if (username != null) {
                    int score = calculateScore(); // Убрали playerCastle
                    String mapName = (currentMapName != null) ? currentMapName : "Standard";
                    try {
                        Leaderboard leaderboard = Leaderboard.loadFromFile();
                        leaderboard.addRecord(username, score, mapName);
                        leaderboard.saveToFile();
                        System.out.println("New record saved: " + score + " points on map " + mapName);
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Failed to save record: " + e.getMessage());
                    }
                }
            }
            System.exit(0);
        }
    }

    private int calculateScore() { // Убрали параметр playerCastle
        Hero playerHero = null;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world[i][j] instanceof Hero hero && hero.isPlayer) {
                    playerHero = hero;
                    break;
                }
            }
            if (playerHero != null) break;
        }
        if (playerHero == null) return 0;
        int score = 10000 - globalTurnCounter * 50 + playerHero.gold * 10;
        return Math.max(0, score);
    }

    public Props[][] endCastleBattle(Hero playerHero, int newY, int newX) {
        if (compIsAlive) {
            world[newY][newX] = new BotCastle();
            background[newY][newX] = new Trail();
            world[playerHero.heroY][playerHero.heroX] = playerHero;
        } else {
            int reward = ((Building) world[newY][newX]).aegis.gold;
            world[newY][newX] = new PlayerCastle();
            background[newY][newX] = new Trail();
            if (playerHero.isPlayer) {
                world[playerHero.heroY][playerHero.heroX] = playerHero;
                System.out.println("You received " + reward + " gold from the enemy!");
                playerHero.gold += reward;
            } else {
                world[playerHero.heroY][playerHero.heroX] = background[playerHero.heroY][playerHero.heroX];
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
        world[computerHero.heroY][computerHero.heroX] = background[computerHero.heroY][computerHero.heroX];
        world[playerHero.heroY][playerHero.heroX] = playerHero;
        isBattleStarted = false;
        playerHero.resetMoves();
        if (username != null) {
            autoSave(new Hero[]{playerHero, computerHero}, "bot_win");
        }
        updateDisplay();
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

    private void loadFromFile(String mapFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/maps/" + mapFile + ".map"));
        String line = reader.readLine();
        String[] sizes = line.split(" ");
        int h = Integer.parseInt(sizes[0]);
        int w = Integer.parseInt(sizes[1]);
        if (h != height || w != width) {
            throw new IOException("Map size mismatch: expected 10x10");
        }
        for (int i = 0; i < height; i++) {
            line = reader.readLine();
            for (int j = 0; j < width; j++) {
                char type = line.charAt(j);
                Props terrain = switch (type) {
                    case 'T' -> new Trail();
                    case 'F' -> new Field();
                    case 'O' -> new Obstacle();
                    default -> throw new IOException("Unknown terrain type: " + type);
                };
                world[i][j] = terrain;
                background[i][j] = terrain;
            }
        }
        reader.close();
    }

    public void saveToFile(String fileName) throws IOException {
        File dir = new File("src/main/resources/maps/");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create maps directory");
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getPath() + "/" + fileName + ".map"));
        writer.write(height + " " + width + "\n");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Props prop = background[i][j];
                char type = switch (prop) {
                    case Trail _ -> 'T';
                    case Obstacle _ -> 'O';
                    case Field _ -> 'F';
                    default -> throw new IOException("Unexpected terrain type in background at (" + i + "," + j + "): " + prop.getClass().getSimpleName());
                };
                writer.write(type);
            }
            writer.write("\n");
        }
        writer.close();
    }

    public void editMode(Scanner scanner) {
        if (world[0][0] == null) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    world[i][j] = new Field();
                    background[i][j] = new Field();
                }
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (i == height / 2 && j != 0 && j != width - 1) {
                        world[i][j] = new Trail();
                        background[i][j] = new Trail();
                    } else if ((j == width * 3 / 8 && i <= height / 2) || (j == width * 5 / 8 && i >= height / 2) || i == height / 5) {
                        world[i][j] = new Trail();
                        background[i][j] = new Trail();
                    }
                }
            }
        }

        int cursorY = height / 2;
        int cursorX = width / 2;

        while (true) {
            updateEditDisplay(cursorY, cursorX);
            System.out.println("Editor: WASD - move cursor, T - Trail, O - Obstacle, F - Field, E - save, Q - quit");

            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "w" -> cursorY = Math.max(0, cursorY - 1);
                case "s" -> cursorY = Math.min(height - 1, cursorY + 1);
                case "a" -> cursorX = Math.max(0, cursorX - 1);
                case "d" -> cursorX = Math.min(width - 1, cursorX + 1);
                case "t" -> setTerrain(cursorY, cursorX, new Trail());
                case "o" -> setTerrain(cursorY, cursorX, new Obstacle());
                case "f" -> setTerrain(cursorY, cursorX, new Field());
                case "e" -> {
                    try {
                        if (currentMapName != null) {
                            // Editing existing map: save under original name without prompt
                            saveToFile(currentMapName);
                            System.out.println("Map updated and saved as " + currentMapName + ".map");
                        } else {
                            // New map: prompt for name
                            System.out.print("Enter map name: ");
                            String name = scanner.nextLine().trim();
                            saveToFile(name);
                            System.out.println("Map saved as " + name + ".map");
                        }
                    } catch (IOException e) {
                        System.out.println("Error saving map: " + e.getMessage());
                    }
                }
                case "q" -> {
                    return;
                }
                default -> System.out.println("Invalid input");
            }
        }
    }

    private void setTerrain(int y, int x, Props terrain) {
        if ((y == height / 2 && x == 0) || (y == height / 2 && x == width - 1)) {
            System.out.println("Cannot edit castle positions!");
            return;
        }
        world[y][x] = terrain;
        background[y][x] = terrain;
    }

    private void updateEditDisplay(int cursorY, int cursorX) {
        clearConsole();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == cursorY && j == cursorX) {
                    System.out.print("@ ");
                } else {
                    if (i == height / 2 && j == 0) {
                        System.out.print(new PlayerCastle() + " ");
                    } else if (i == height / 2 && j == width - 1) {
                        System.out.print(new BotCastle() + " ");
                    } else {
                        System.out.print(world[i][j] + " ");
                    }
                }
            }
            System.out.println();
        }
    }

    public void saveGame(String fileName, Hero[] heroes) throws IOException {
        if (username == null) {
            System.out.println("Cannot save without username");
            return;
        }
        File dir = new File("src/main/saves/" + username);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create saves directory");
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dir.getPath() + "/" + fileName + ".sav"))) {
            oos.writeObject(this);
            oos.writeObject(heroes);
            oos.writeObject(username);
        }
    }

    public static AreaMap loadGame(String fileName, String username, Hero[] heroes) throws IOException, ClassNotFoundException {
        File saveFile = new File("src/main/saves/" + username + "/" + fileName + ".sav");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            AreaMap map = (AreaMap) ois.readObject();
            Hero[] loadedHeroes = (Hero[]) ois.readObject();
            String loadedUsername = (String) ois.readObject();
            if (!loadedUsername.equals(username)) {
                throw new IOException("This save is not for user " + username);
            }
            heroes[0] = loadedHeroes[0];
            heroes[1] = loadedHeroes[1];
            // Re-init transient
            currentMap = map;
            return map;
        }
    }

    private void autoSave(Hero[] heroes, String event) {
        try {
            saveGame("auto_save_" + event + "_" + System.currentTimeMillis(), heroes);
            System.out.println("Auto-saved after " + event);
        } catch (IOException e) {
            System.out.println("Auto-save failed: " + e.getMessage());
        }
    }

    public void endRound(Hero[] heroes) {
        for (Hero hero : heroes) {
            if (hero.isAvailable && !isBattleStarted) {
                hero.resetMoves();
            }
        }
        if (username != null) {
            autoSave(heroes, "end_round");
        }
    }

    public boolean endGame() {
        if (world[height/2][0] instanceof PlayerCastle &&
                world[height/2][width-1] instanceof PlayerCastle) {
            System.out.println("You won!");
            return true;
        }
        if (world[height/2][0] instanceof BotCastle &&
                world[height/2][width-1] instanceof BotCastle) {
            System.out.println("You lost!");
            return true;
        }
        return false;
    }


}