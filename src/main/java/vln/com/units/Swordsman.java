package vln.com.units;

import java.io.Serializable;

public class Swordsman extends Unit implements Serializable {

    public Swordsman(int count, boolean isSalonUp, boolean isHotelUp) {
        this.cost = 10;
        this.count = count;
        this.HP = 15;
        this.damage = 7;
        this.distance = 1;
        this.movement = 3;
        this.design = "S";
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