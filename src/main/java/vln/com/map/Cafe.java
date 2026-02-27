package vln.com.map;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Cafe extends Props implements Serializable {
    public boolean isVisited = false;

    public Cafe() {
        this.design = "♨";
    }
}