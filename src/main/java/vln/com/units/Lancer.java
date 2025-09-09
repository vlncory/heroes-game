package vln.com.units;

import java.io.Serializable;

public class Lancer extends Unit implements Serializable {

    public Lancer(int count) {
        this.cost = 5;
        this.count = count;
        this.HP = 7;
        this.damage = 3;
        this.stackHP = this.HP * this.count;
        this.stackDamage = this.damage * this.count;
        this.distance = 1;
        this.movement = 2;
        this.design = "L";
    }
}