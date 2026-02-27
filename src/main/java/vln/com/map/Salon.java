package vln.com.map;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Salon extends Props implements Serializable {
    public boolean isVisited = false;

    public Salon() {
        this.design = "❦";
    }
}