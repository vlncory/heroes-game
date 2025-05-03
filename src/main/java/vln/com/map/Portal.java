package vln.com.map;

import vln.com.graphic.Props;

public class Portal extends Props {

    public int turnsSinceSpawn = 0;

    public Portal() {
        this.design = "\u001B[35m" + "◎" + "\u001B[0m";
    }
}