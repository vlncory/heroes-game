package vln.com.pattern;

import vln.com.graphic.Props;

public class Smoke extends Props {

    public Props originalProp;

    public Smoke(Props original) {
        this.design = "\u001B[37m" + "▒" + "\u001B[0m";
        this.originalProp = original;
    }
}