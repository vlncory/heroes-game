package vln.com.map;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Hotel extends Props implements Serializable {
    public boolean isVisited = false;

    public Hotel() {
        this.design = "☗";
    }
}