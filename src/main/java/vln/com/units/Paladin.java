package vln.com.units;

import java.io.Serializable;

public class Paladin extends Unit implements Serializable {

    public Paladin(int count, boolean isSalonUp, boolean isHotelUp) {
        this.cost = 25;
        this.count = count;
        this.HP = 18;
        this.damage = 10;
        this.distance = 2;
        this.movement = 5;
        this.design = "P";
        if (isSalonUp) {
            this.HP += 1;
        }
        if (isHotelUp) {
            this.damage += 1;
        }
        this.stackHP = this.HP * this.count;
        this.stackDamage = this.damage * this.count;
    }
}