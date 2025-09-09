package vln.com.pattern;

import vln.com.graphic.Props;

import java.io.Serializable;

public class Smoke extends Props implements Serializable {

    public Props originalProp;

    public Smoke(Props original) {
        this.design = "\u001B[37m" + "▒" + "\u001B[0m";
        this.originalProp = original;
    }
}