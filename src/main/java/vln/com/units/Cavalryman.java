package vln.com.units;

import java.io.Serializable;

public class Cavalryman extends Unit implements Serializable {

    public Cavalryman(int count) {
        this.cost = 15;
        this.count = count;
        this.HP = 12;
        this.damage = 9;
        this.stackHP = this.HP * this.count;
        this.stackDamage = this.damage * this.count;
        this.distance = 2;
        this.movement = 4;
        this.design = "C";
    }
}