package vln.com.units;

import vln.com.graphic.Props;
import vln.com.buildings.Building;

import java.io.Serializable;
import java.util.HashMap;

public class Hero extends Props implements Serializable {

    public int gold;
    public int heroX;
    public int heroY;
    public HashMap<String, Integer> army;
    public boolean isStable = false;
    public boolean isPlayer = false;
    public boolean isAvailable = false;
    public int moves = 10;

    public Hero(int gold) {
        this.gold = gold;
        this.army = new HashMap<>();
    }

    public void setSymbol(String symbol) {
        this.design = symbol;
    }

    public void resetMoves() {
        if (isStable) {
            System.out.println("Restored 15 moves");
            this.moves = 15;
        } else {
            System.out.println("Restored 10 moves");
            this.moves = 10;
        }
    }

    public void unitPurchase(Building castle, String choice, int gold) {
        switch (choice) {
            case "1" -> BuyLancer(gold, castle);
            case "2" -> BuyArcher(gold, castle);
            case "3" -> BuySwordsman(gold, castle);
            case "4" -> BuyCavalryman(gold, castle);
            case "5" -> BuyPaladin(gold, castle);
            default -> System.out.println("Invalid choice.");
        }
    }

    void BuyLancer(int gold, Building castle) {
        if (castle.isBuildingPurchased("G") && castle.isBuildingPurchased("T")) {
            Lancer guy = new Lancer(0);
            processPurchase(guy, gold, "Lancer");
        } else {
            System.out.println("Need both Guard post and Tavern to recruit Lancers!");
        }
    }

    void BuyArcher(int gold, Building castle) {
        if (castle.isBuildingPurchased("A") && castle.isBuildingPurchased("T")) {
            Archer guy = new Archer(0);
            processPurchase(guy, gold, "Archer");
        } else {
            System.out.println("Need both Archer Tower and Tavern to recruit Archers!");
        }
    }

    void BuySwordsman(int gold, Building castle) {
        if (castle.isBuildingPurchased("W") && castle.isBuildingPurchased("T")) {
            Swordsman guy = new Swordsman(0);
            processPurchase(guy, gold, "Swordsman");
        } else {
            System.out.println("Need both Weapon Shop and Tavern to recruit Swordsmen!");
        }
    }

    void BuyCavalryman(int gold, Building castle) {
        if (castle.isBuildingPurchased("AA") && castle.isBuildingPurchased("T")) {
            Cavalryman guy = new Cavalryman(0);
            processPurchase(guy, gold, "Cavalryman");
        } else {
            System.out.println("Need both Arena and Tavern to recruit Cavalrymen!");
        }
    }

    void BuyPaladin(int gold, Building castle) {
        if (castle.isBuildingPurchased("C") && castle.isBuildingPurchased("T")) {
            Paladin guy = new Paladin(0);
            processPurchase(guy, gold, "Paladin");
        } else {
            System.out.println("Need both Cathedral and Tavern to recruit Paladins!");
        }
    }

    private void processPurchase(Unit unit, int availableGold, String unitName) {
        int maxUnits = availableGold / unit.cost;
        if (maxUnits > 0) {
            this.gold -= maxUnits * unit.cost;
            army.put(unitName, army.getOrDefault(unitName, 0) + maxUnits);
            System.out.printf("Bought %d %s%n", maxUnits, unitName.toLowerCase());
        } else {
            System.out.printf("Not enough gold to buy %s%n", unitName.toLowerCase());
        }
    }
}