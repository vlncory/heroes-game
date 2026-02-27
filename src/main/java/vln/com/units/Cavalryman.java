package vln.com.units;

public class Cavalryman extends Unit {

    public Cavalryman(int count, boolean isSalonUp, boolean isHotelUp) {
        this.cost = 15;
        this.count = count;
        this.HP = 12;
        this.damage = 9;
        this.distance = 2;
        this.movement = 4;
        this.design = "C";
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