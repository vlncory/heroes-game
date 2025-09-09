package vln.com.units;

import java.io.Serializable;

public class Swordsman extends Unit implements Serializable {

    public Swordsman(int count) {
        this.cost = 10;
        this.count = count;
        this.HP = 15;
        this.damage = 7;
        this.stackHP = this.HP * this.count;
        this.stackDamage = this.damage * this.count;
        this.distance = 1;
        this.movement = 3;
        this.design = "S";
    }
}