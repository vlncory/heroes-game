package vln.com.map;

import java.io.Serializable;

public class Trail extends Road implements Serializable {

    public Trail() {
        this.path = 1;
        this.design = "▒";
    }
}