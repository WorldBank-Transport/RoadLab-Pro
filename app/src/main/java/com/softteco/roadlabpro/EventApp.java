package com.softteco.roadlabpro;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vadim Alenin on 5/15/2015.
 */
public class EventApp {

    /**
     * Unique key.
     */
    public static final String SYNC_ALL = "SYNC_ALL";

    /**
     * Unique key.
     */
    public static final String SYNC_ONLY_ISSUE = "SYNC_ONLY_ISSUE";

    /**
     * Unique key.
     */
    public static final String SYNC_ONLY_ROAD_INTERVAL = "SYNC_ONLY_ROAD_INTERVAL";

    /**
     * The table for all event applications within one session.
     *
     * @see {@link java.util.HashMap}
     */
    private Map<String, Object> sessionMap = new HashMap<>();

    /**
     * The method creates a new event application.
     *
     * @param id    the id
     * @param value the value
     */
    public void setValue(final String id, final Object value) {
        sessionMap.put(id, value);
    }

    /**
     * The method gets event without removes from @see #sessionMap.
     *
     * @param id the id
     * @return event application
     */
    public Object getValue(final String id) {
        return getValue(id, false);
    }

    /**
     * The method gets event with removes from @see #sessionMap.
     *
     * @param id     the id
     * @param remove true if need remove event from #sessionMap, false otherwise.
     * @return event application
     */
    public Object getValue(final String id, final boolean remove) {
        return remove ? sessionMap.remove(id) : sessionMap.get(id);
    }

    /**
     * The method removes all event in the @see #sessionMap.
     */
    public void clear() {
        sessionMap.clear();
    }
}
