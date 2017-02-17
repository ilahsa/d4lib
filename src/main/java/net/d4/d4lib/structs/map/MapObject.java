package net.d4.d4lib.structs.map;

import net.d4.d4lib.structs.BaseObject;

/**
 * 地图对象
 *
 */
public class MapObject extends BaseObject {

    private static final long serialVersionUID = 186006352516787595L;

    private float x;
    private float y;
    private float z;

    //地图魔板id
    private int mapModelId;
    //地图id
    private long mapId;
    //线路id
    private int lineId;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public int getMapModelId() {
        return mapModelId;
    }

    public void setMapModelId(int mapModelId) {
        this.mapModelId = mapModelId;
    }

    public long getMapId() {
        return mapId;
    }

    public void setMapId(long mapId) {
        this.mapId = mapId;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

}
