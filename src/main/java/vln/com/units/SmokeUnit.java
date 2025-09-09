package vln.com.units;

import java.util.Random;

import vln.com.graphic.Props;
import vln.com.pattern.AreaMap;

import java.io.Serializable;

public class SmokeUnit extends Props implements Serializable {

    private final int smokeCenterY;
    private final int smokeCenterX;

    public SmokeUnit(AreaMap map) {
        Random rand = new Random();
        this.smokeCenterY = 2 + rand.nextInt(map.getHeight() - 4);
        this.smokeCenterX = 2 + rand.nextInt(map.getWidth() - 4);
        this.design = "\u001B[37m☁\u001B[0m";
        map.applySmoke(smokeCenterY, smokeCenterX);
    }

    public int getSmokeCenterY() {
        return smokeCenterY;
    }

    public int getSmokeCenterX() {
        return smokeCenterX;
    }
}