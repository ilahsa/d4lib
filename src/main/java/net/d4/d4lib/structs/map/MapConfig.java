package net.d4.d4lib.structs.map;

/**
 *
 */
public class MapConfig {

    private int mapWidth;
    private int mapHeight;

    private int areaWidth;
    private int areaHeight;

    private byte[][] blocks;

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getAreaWidth() {
        return areaWidth;
    }

    public void setAreaWidth(int areaWidth) {
        this.areaWidth = areaWidth;
    }

    public int getAreaHeight() {
        return areaHeight;
    }

    public void setAreaHeight(int areaHeight) {
        this.areaHeight = areaHeight;
    }

    public byte[][] getBlocks() {
        return blocks;
    }

    public void setBlocks(byte[][] blocks) {
        this.blocks = blocks;
    }

}
