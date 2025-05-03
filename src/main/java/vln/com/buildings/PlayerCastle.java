package vln.com.buildings;

import vln.com.units.Hero;

public class PlayerCastle extends Building {

    public PlayerCastle() {
        aegis = new Hero(100);
        aegis.isPlayer = true;
        aegis.army.put("Archer", 4);
        this.isPlayerTower = true;
        this.design = "\u001B[34m" + "⏏" + "\u001B[0m";
    }
}