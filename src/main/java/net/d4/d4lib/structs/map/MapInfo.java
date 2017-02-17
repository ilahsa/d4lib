package net.d4.d4lib.structs.map;

import java.util.ArrayList;

/**
 *
 * @param <TPlayer>
 * @param <TNpc>
 * @param <TMonster>
 * @param <TDrop>
 */
public class MapInfo<TPlayer extends MapObject, TNpc extends MapObject, TMonster extends MapObject, TDrop extends MapObject> {

    private Area[][] areas;

    private MapConfig mapConfig;

    public MapInfo(MapConfig config) {
        this.mapConfig = config;
        int aw = mapConfig.getMapWidth() / mapConfig.getAreaWidth() + 1;
        int ah = mapConfig.getMapHeight() / mapConfig.getAreaHeight() + 1;
        this.areas = new Area[ah][aw];
        for (int i = 0; i < ah; i++) {
            for (int j = 0; j < aw; j++) {
                areas[i][j] = new Area<>();
            }
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<TPlayer> getPlayers() {
        return new ArrayList<>();
    }

    public ArrayList<Area> getRoundAreas(MapObject object) {
        ArrayList<Area> retAreas = new ArrayList<>();
//        float aw = (mapConfig.getAreaWidth() * 1.5f);
//        if (object.getX() < mapConfig.getAreaWidth()) {
//
//        }
//
//        int aw = 0;
//        if (mapConfig.getMapWidth() - (mapConfig.getAreaWidth() * 1.5) > object.getX()) {
//            aw = (int) object.getX() / mapConfig.getAreaWidth() + ((int) object.getX() % mapConfig.getAreaWidth() > 0 ? 1 : 0);
//        } else {
//            aw = mapConfig.getMapWidth() / mapConfig.getAreaWidth() + ((int) object.getX() % mapConfig.getAreaWidth() > 0 ? 1 : 0) - 1;
//        }
//
//        int ah = (int) object.getZ() / mapConfig.getAreaHeight() + 1;
//
//        for (int i = ah - 1; i <= ah + 1; i++) {
//            for (int j = aw - 1; j <= aw + 1; j++) {
//                retAreas.add(areas[i][j]);
//            }
//        }
        return retAreas;
    }

    public ArrayList<TPlayer> getRoundPlayers(MapObject object) {
        ArrayList<Area> roundAreas = getRoundAreas(object);
        for (Area roundArea : roundAreas) {

        }
        return new ArrayList<>();
    }

}
