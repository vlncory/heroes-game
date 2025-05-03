package vln.com.buildings;

import vln.com.units.Hero;
import vln.com.map.AreaMap;
import vln.com.graphic.Props;

public class Building extends Props {

    public Hero aegis;
    public boolean isPlayerTower;

    private enum BuildingType {
        TAVERN("T", "Tavern", 15),
        STABLE("S", "Stable", 20),
        GUARD_POST("G", "Guard post", 13),
        ARCHER_TOWER("A", "Archer tower", 16),
        WEAPON_SHOP("W", "Weapon shop", 20),
        ARENA("AA", "Arena", 25),
        CATHEDRAL("C", "Cathedral", 30),
        LOCATOR("L", "Locator", 70);

        final String key;
        final String name;
        final int cost;
        boolean purchased;

        BuildingType(String key, String name, int cost) {
            this.key = key;
            this.name = name;
            this.cost = cost;
            this.purchased = false;
        }
    }

    public void printBuildings() {
        System.out.println("Available buildings:");
        for (BuildingType type : BuildingType.values()) {
            System.out.println(getBuildingMessage(type));
        }
        System.out.println("Enter 'Q' to exit");

    }

    private String getBuildingMessage(BuildingType type) {
        if (type.purchased) {
            return String.format("The %s has already been purchased", type.name);
        }
        return String.format("Buy %s for %d gold? (%s)", type.name, type.cost, type.key);
    }

    public void handleBuildingPurchase(Hero hero, String choice) {
        for (BuildingType type : BuildingType.values()) {
            if (type.key.equalsIgnoreCase(choice)) {
                purchaseBuilding(hero, type);
                return;
            }
        }
        System.out.println("Invalid choice.");
    }

    private void purchaseBuilding(Hero hero, BuildingType type) {
        if (type.purchased) {
            System.out.println(type.name + " already purchased");
            return;
        }

        if (hero.gold >= type.cost) {
            hero.gold -= type.cost;
            type.purchased = true;
            handleSpecialCases(type, hero);
            System.out.println(type.name + " purchased successfully!");
        } else {
            System.out.println("Not enough gold for " + type.name);
        }
    }

    public boolean isBuildingPurchased(String key) {
        for (BuildingType type : BuildingType.values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type.purchased;
            }
        }
        return false;
    }

    private void handleSpecialCases(BuildingType type, Hero hero) {
        if (type == BuildingType.STABLE) {
            hero.isStable = true;
        } else if (type == BuildingType.LOCATOR) {
            if (hero.isPlayer && AreaMap.currentMap != null) {
                AreaMap.currentMap.revealSmokeWithLocator();
            }
        }
    }
}