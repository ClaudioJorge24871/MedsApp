package com.example.medsapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Device {
    private String id;
    private String title;
    private String description;


    public Device(String id, String title, String description){
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Convert to JSON
     * @return
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException{
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        return obj;
    }

    /**
     * Convert from JSON
     * @param obj
     * @return
     * @throws JSONException
     */
    public static Device fromJSON(JSONObject obj) throws JSONException {
        return new Device(
                obj.getString("id"),
                obj.getString("title"),
                obj.getString("description")
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Device device = (Device) obj;
        return id.equals(device.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }




}
