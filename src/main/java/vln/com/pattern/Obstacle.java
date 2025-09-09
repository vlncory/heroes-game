package vln.com.pattern;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Obstacle extends Props implements Serializable {

    public Obstacle() {
        this.design = "\u001B[33m" + "♧" + "\u001B[0m";
    }
}