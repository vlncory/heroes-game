package vln.com.units;

public class Archer extends Unit {

    public Archer(int count, boolean isSalonUp, boolean isHotelUp) {
        this.cost = 8;
        this.count = count;
        this.HP = 5;
        this.damage = 5;
        this.distance = 100;
        this.movement = 1;
        this.design = "A";
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