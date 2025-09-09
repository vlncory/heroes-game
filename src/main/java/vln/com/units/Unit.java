package vln.com.units;

import java.util.Scanner;

import vln.com.graphic.Props;
import vln.com.pattern.Trail;

public class Unit extends Props {

    protected int cost;
    public int HP;
    public int damage;
    public int stackHP;
    public int stackDamage;
    public int count;
    public int distance;
    public int movement;
    public int unitX;
    public int unitY;
    public boolean relocated = false;
    public boolean isPlayerUnit;

    public void update() {
        this.stackDamage = this.count * this.damage;
    }

    public void casualties(int damage) {
        int unitLost = damage / this.HP;
        int remainDamage = damage % this.HP;

        this.count = Math.max(0, this.count - unitLost);
        this.stackHP -= unitLost * this.HP;

        if (this.count > 0 && remainDamage > 0) {
            this.stackHP -= remainDamage;
            if (this.stackHP <= 0) {
                this.count--;
                this.stackHP = 0;
            }
        }

        update();
    }

    public void act(Props[][] battlefield, Scanner scanner) {
        while (true) {
            System.out.println("Health of one unit - " + this.HP);
            System.out.println("Damage of one unit - " + this.damage);
            System.out.println("Maximum health of the unit - " + this.stackHP);
            System.out.println("Maximum damage of the unit - " + this.stackDamage);
            System.out.println("Maximum attack range of the unit - " + this.distance);
            System.out.println("Maximum movement range - " + this.movement);
            System.out.println();

            boolean canAttack = findUnits(battlefield);

            System.out.println("To move press 1");
            if (canAttack) {
                System.out.println("To attack press 2");
            }
            System.out.print("Choose an action: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Error: enter a number");
                scanner.nextLine();
                continue;
            }
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    while (true) {
                        System.out.print("Enter direction (WASD): ");
                        String direction = scanner.nextLine().trim().toUpperCase();
                        if (!direction.matches("[WASD]")) {
                            System.out.println("Invalid direction. Please enter W, A, S, D");
                            System.out.println();
                            continue;
                        }
                        moveUnit(battlefield, direction);
                        break;
                    }
                    return;
                }
                case 2 -> {
                    if (!canAttack) {
                        System.out.println("You cannot attack: no enemies in range");
                        System.out.println();
                        continue;
                    }
                    attackUnit(battlefield, scanner);
                    return;
                }
                default -> {
                    System.out.println("Invalid choice. Enter 1" + (canAttack ? " or 2!" : "!"));
                    System.out.println();
                }
            }
        }
    }

    private void moveUnit(Props[][] battlefield, String direction) {
        if (this.relocated) {
            System.out.println("This unit has already moved this turn");
            return;
        }

        int newX = this.unitX;
        int newY = this.unitY;
        int stepsRemaining = this.movement;

        while (stepsRemaining > 0) {
            int nextX = newX;
            int nextY = newY;

            switch (direction) {
                case "W" -> nextY = Math.max(0, newY - 1);
                case "A" -> nextX = Math.max(0, newX - 1);
                case "S" -> nextY = Math.min(battlefield.length - 1, newY + 1);
                case "D" -> nextX = Math.min(battlefield[0].length - 1, newX + 1);
                default -> {
                    System.out.println("Invalid direction");
                    System.out.println();
                    return;
                }
            }

            if (battlefield[nextY][nextX] instanceof Trail) {
                battlefield[newY][newX] = new Trail();
                newX = nextX;
                newY = nextY;
                battlefield[newY][newX] = this;
                stepsRemaining--;
            } else {
                break;
            }
        }

        this.unitX = newX;
        this.unitY = newY;
        this.relocated = true;

        System.out.println("Unit moved to (" + newY + ", " + newX + ")");
    }

    private boolean findUnits(Props[][] battlefield) {
        boolean hasEnemies = false;
        int minRow = Math.max(0, this.unitY - this.distance);
        int maxRow = Math.min(battlefield.length - 1, this.unitY + this.distance);
        int minCol = Math.max(0, this.unitX - this.distance);
        int maxCol = Math.min(battlefield[0].length - 1, this.unitX + this.distance);

        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                if (battlefield[i][j] instanceof Unit target && battlefield[i][j] != this) {
                    if (!target.isPlayerUnit) {
                        System.out.println("Target: (" + i + ", " + j + ") - " + target.design);
                        hasEnemies = true;
                    }
                }
            }
        }
        return hasEnemies;
    }

    private void attackUnit(Props[][] battlefield, Scanner scanner) {
        System.out.println("Choose a target to attack:");
        findUnits(battlefield);

        while (true) {
            System.out.print("Enter target coordinates (row col): ");
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Error: please enter two numbers separated by a space.");
                System.out.println();
                continue;
            }

            int targetRow;
            int targetCol;
            try {
                targetRow = Integer.parseInt(parts[0]);
                targetCol = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                System.out.println("Error: both coordinates must be integers.");
                System.out.println();
                continue;
            }

            if (targetRow < 0 || targetRow >= battlefield.length || targetCol < 0 || targetCol >= battlefield[0].length) {
                System.out.println("Invalid target coordinates: out of bounds.");
                System.out.println();
                continue;
            }

            if (Math.abs(this.unitY - targetRow) > this.distance || Math.abs(this.unitX - targetCol) > this.distance) {
                System.out.println("Target is out of range.");
                System.out.println();
                continue;
            }

            Props p = battlefield[targetRow][targetCol];
            if (!(p instanceof Unit target) || target.isPlayerUnit) {
                System.out.println("There is no enemy unit at those coordinates.");
                System.out.println();
                continue;
            }

            System.out.println("Our " + this.design + " attacks " + target.design + " at (" + targetRow + ", " + targetCol + ")!");
            System.out.println("Damage dealt: " + this.stackDamage);
            target.casualties(this.stackDamage);
            System.out.println("The " + target.design + " has " + target.stackHP + " health left");
            if (target.count <= 0) {
                battlefield[targetRow][targetCol] = new Trail();
                System.out.println(target.design + " destroyed!");
            }
            break;
        }
    }
}