package vln.com.map;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Portal extends Props implements Serializable {

    public int turnsSinceSpawn = 0;

    public Portal() {
        this.design = "\u001B[35m" + "◎" + "\u001B[0m";
    }
}