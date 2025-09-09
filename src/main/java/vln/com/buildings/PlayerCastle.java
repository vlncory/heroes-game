package vln.com.buildings;

import vln.com.units.Hero;

import java.io.Serializable;

public class PlayerCastle extends Building implements Serializable {

    public PlayerCastle() {
        aegis = new Hero(100);
        aegis.isPlayer = true;
        aegis.army.put("Archer", 4);
        this.isPlayerTower = true;
        this.design = "\u001B[34m" + "⏏" + "\u001B[0m";
    }
}