package vln.com.units;

public class Lancer extends Unit {

    public Lancer(int count, boolean isSalonUp, boolean isHotelUp) {
        this.cost = 5;
        this.count = count;
        this.HP = 7;
        this.damage = 3;
        this.distance = 1;
        this.movement = 2;
        this.design = "L";
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