package vln.com.buildings;

import vln.com.units.Hero;

import java.io.Serializable;

public class BotCastle extends Building implements Serializable {

    public BotCastle() {
        aegis = new Hero(100);
        aegis.isPlayer = false;
        aegis.army.put("Archer", 4);
        this.isPlayerTower = false;
        this.design = "\u001B[31m" + "⏏" + "\u001B[0m";
    }
}