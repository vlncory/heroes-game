package vln.com.map;

import java.io.Serializable;

public class Field extends Road implements Serializable {

    public Field() {
        this.path = 2;
        this.design = "\u001B[32m" + "▦" + "\u001B[0m";
    }
}