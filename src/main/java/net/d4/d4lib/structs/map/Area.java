package net.d4.d4lib.structs.map;

import java.util.ArrayList;

/**
 *
 * @param <TPlayer>
 * @param <TNpc>
 * @param <TMonster>
 * @param <TDrop>
 */
public class Area<TPlayer extends MapObject, TNpc extends MapObject, TMonster extends MapObject, TDrop extends MapObject> {

    private ArrayList<TPlayer> players = new ArrayList<>();
    private ArrayList<TNpc> npcs = new ArrayList<>();
    private ArrayList<TMonster> monsters = new ArrayList<>();
    private ArrayList<TDrop> drops = new ArrayList<>();

    public Area() {
    }

    public ArrayList<TPlayer> getPlayers() {
        return players;
    }

    public ArrayList<TNpc> getNpcs() {
        return npcs;
    }

    public ArrayList<TMonster> getMonsters() {
        return monsters;
    }

    public ArrayList<TDrop> getDrops() {
        return drops;
    }

}
