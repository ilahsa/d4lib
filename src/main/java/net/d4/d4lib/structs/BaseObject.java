/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.d4.d4lib.structs;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 */
@Entity
public class BaseObject implements Serializable {

    private static final long serialVersionUID = -8981799590065464386L;

    //id
    @Id
    private long id;
    //服务器id
    private int serverId;
    //分区id
    private int zonesid;

    public BaseObject() {

        id = ObjectGlobal.getInstance().getCreateId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getZonesid() {
        return zonesid;
    }

    public void setZonesid(int zonesid) {
        this.zonesid = zonesid;
    }

}
