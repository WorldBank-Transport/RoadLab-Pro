package com.softteco.roadlabpro.sqlite.model;

import java.io.Serializable;

/**
 * Created by ppp on 15.04.2015.
 */
public class BaseModel implements Serializable {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
