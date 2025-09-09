package vln.com.battle;

import java.util.Map;
import java.util.Scanner;

import vln.com.graphic.Props;
import vln.com.pattern.*;
import vln.com.units.*;

public class FieldOfHonor {

    private static final int HEIGHT = 5;
    private static final int WIDTH = 10;
    private static final int CONSOLE_CLEAR_LINES = 25;
    private static final int DISPLAY_DELAY_MS = 500;
    private boolean testingMode = false;

    private final Props[][] battleGrid = new Props[HEIGHT][WIDTH];

    public void setTestingMode(boolean testingMode) {
        this.testingMode = testingMode;
    }

    public FieldOfHonor(Hero hero1, Hero hero2) {
        initializeBattleField();
        placeHeroUnits(hero1, hero2);
        updateMap();
    }

    private void initializeBattleField() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                battleGrid[i][j] = new Trail();
            }
        }
    }

    private void placeHeroUnits(Hero hero1, Hero hero2) {
        if (hero1.isPlayer) {
            placeUnits(hero1, 0);
            placeUnits(hero2, WIDTH - 1);
        } else {
            placeUnits(hero2, 0);
            placeUnits(hero1, WIDTH - 1);
        }
    }

    private void placeUnits(Hero hero, int startColumn) {
        int row = 0;

        for (Map.Entry<String, Integer> entry : hero.army.entrySet()) {
            if (row >= HEIGHT) break;

            String unitType = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                Unit unit = createUnit(unitType, count);
                unit.unitX = startColumn;
                unit.unitY = row;
                unit.isPlayerUnit = hero.isPlayer;
                battleGrid[row][startColumn] = unit;
                row++;
            }
        }
    }

    public void startBattle() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (handlePlayerTurn(scanner)) break;
            if (isBattleOver()) break;

            if (handleEnemyTurn()) break;
            if (isBattleOver()) break;
        }
    }

    private boolean handlePlayerTurn(Scanner scanner) {
        System.out.println("\n=== YOUR TURN ===");
        resetMovementFlags();
        boolean battleEnded = processUnits(true, scanner);
        updateMap();
        return battleEnded;
    }

    private boolean handleEnemyTurn() {
        System.out.println("\n=== ENEMY'S TURN ===");
        resetMovementFlags();
        boolean battleEnded = processUnits(false, null);
        updateMap();
        return battleEnded;
    }

    private boolean processUnits(boolean isPlayer, Scanner scanner) {
        for (Props[] row : battleGrid) {
            for (Props cell : row) {
                if (cell instanceof Unit unit) {
                    if (unit.isPlayerUnit == isPlayer && !unit.relocated && unit.count > 0) {
                        handleUnitAction(unit, scanner);
                        if (isBattleOver()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void handleUnitAction(Unit unit, Scanner scanner) {
        if (unit.isPlayerUnit) {
            System.out.println();
            System.out.printf("Unit %s at (%d, %d) acting%n", unit.design, unit.unitY, unit.unitX);
            unit.act(battleGrid, scanner);
            updateMap();
        } else {
            executeEnemyUnitAction(unit);
            updateMap();
        }

        unit.relocated = true;
    }

    private void executeEnemyUnitAction(Unit unit) {
        Unit target = findTargetInRange(unit);

        if (target != null) {
            performAttack(unit, target);
        } else {
            moveTowardsPlayer(unit);
        }
    }

    private Unit findTargetInRange(Unit attacker) {
        for (int y = Math.max(0, attacker.unitY - attacker.distance);
             y <= Math.min(HEIGHT - 1, attacker.unitY + attacker.distance); y++) {
            for (int x = Math.max(0, attacker.unitX - attacker.distance);
                 x <= Math.min(WIDTH - 1, attacker.unitX + attacker.distance); x++) {
                if (battleGrid[y][x] instanceof Unit target && target.isPlayerUnit) {
                    System.out.println();
                    System.out.printf("Enemy %s attacking your %s%n", attacker.design, target.design);
                    return target;
                }
            }
        }
        return null;
    }

    private void performAttack(Unit attacker, Unit target) {
        target.casualties(attacker.stackDamage);
        System.out.printf("Dealt %d damage to %s (Remaining HP: %d)%n",
                attacker.stackDamage, target.design, target.stackHP);

        if (target.stackHP <= 0) {
            battleGrid[target.unitY][target.unitX] = new Trail();
            System.out.println(target.design + " destroyed!");
        }
    }

    private void moveTowardsPlayer(Unit unit) {
        int steps = unit.movement;
        int newX = unit.unitX;
        int newY = unit.unitY;

        while (steps-- > 0) {
            int nextX = newX - 1;

            if (nextX < 0 || !(battleGrid[newY][nextX] instanceof Trail)) break;

            battleGrid[newY][newX] = new Trail();
            newX = nextX;
            battleGrid[newY][newX] = unit;
        }

        if (newX != unit.unitX) {
            unit.unitX = newX;
            System.out.printf("%s moved to (%d, %d)%n", unit.design, unit.unitY, unit.unitX);
        }
    }

    private void resetMovementFlags() {
        for (Props[] row : battleGrid) {
            for (Props cell : row) {
                if (cell instanceof Unit unit) {
                    unit.relocated = false;
                }
            }
        }
    }

    private boolean isBattleOver() {
        boolean playerAlive = false;
        boolean compAlive = false;

        for (Props[] row : battleGrid) {
            for (Props cell : row) {
                if (cell instanceof Unit unit) {
                    if (unit.count <= 0) continue;

                    if (unit.isPlayerUnit) {
                        playerAlive = true;
                    } else {
                        compAlive = true;
                    }
                }
            }
        }

        if (!playerAlive) {
            battleResult(false);
            if (!testingMode) {
                System.exit(0);
            }
            return true;
        } else if (!compAlive) {
            battleResult(true);
            AreaMap.compIsAlive = false;
            return true;
        }

        return false;
    }

    private void battleResult(boolean playerWon) {
        System.out.println(playerWon ? "Victory!" : "Defeat...");
    }

    private void clearConsole() {
        try {
            Thread.sleep(DISPLAY_DELAY_MS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < CONSOLE_CLEAR_LINES; i++) {
            System.out.println();
        }
    }

    private void updateMap() {
        clearConsole();

        for (Props[] row : battleGrid) {
            for (Props cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    private Unit createUnit(String unitType, int count) {
        return switch (unitType) {
            case "Lancer" -> new Lancer(count);
            case "Archer" -> new Archer(count);
            case "Swordsman" -> new Swordsman(count);
            case "Cavalryman" -> new Cavalryman(count);
            case "Paladin" -> new Paladin(count);
            default -> throw new IllegalArgumentException("Unknown unit type: " + unitType);
        };
    }
}