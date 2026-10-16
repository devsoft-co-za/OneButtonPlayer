package com.circusscientist.onebuttonplayer;

public class ShowFile {
    private String name;
    private String uri;
    private int index;

    public ShowFile(String name, String uri, int index) {
        this.name = name;
        this.uri = uri;
        this.index = index;
    }

    // Getters
    public String getName() { return name; }
    public String getUri() { return uri; }
    public int getIndex() { return index; }
}
