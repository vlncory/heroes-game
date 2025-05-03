package vln.com.units;

public class Paladin extends Unit {

    public Paladin(int count) {
        this.cost = 25;
        this.count = count;
        this.HP = 18;
        this.damage = 10;
        this.stackHP = this.HP * this.count;
        this.stackDamage = this.damage * this.count;
        this.distance = 2;
        this.movement = 5;
        this.design = "P";
    }
}