package com.circusscientist.onebuttonplayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Show {
    private String id;
    private String name;
    private List<ShowFile> files;
    private List<ShowEvent> events;
    private String created;

    public Show(String id, String name) {
        this.id = id;
        this.name = name;
        this.files = new ArrayList<>();
        this.events = new ArrayList<>();
        this.created = new Date().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public List<ShowFile> getFiles() { return files; }
    public List<ShowEvent> getEvents() { return events; }
    public String getCreated() { return created; }

    public void setName(String name) { this.name = name; }
    public void setFiles(List<ShowFile> files) { this.files = files; }
    public void setEvents(List<ShowEvent> events) { this.events = events; }
}
