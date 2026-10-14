package com.example.onebuttonplayer;

public class ShowEvent {
    private String type; // "start", "stop", "pause", "resume"
    private int trackIndex;
    private String trackName;
    // Only meaningful for "start" events: the track repeats until an event stops it.
    private boolean loop;

    public ShowEvent(String type, int trackIndex, String trackName) {
        this(type, trackIndex, trackName, false);
    }

    public ShowEvent(String type, int trackIndex, String trackName, boolean loop) {
        this.type = type;
        this.trackIndex = trackIndex;
        this.trackName = trackName;
        this.loop = loop;
    }

    // Getters
    public String getType() { return type; }
    public int getTrackIndex() { return trackIndex; }
    public String getTrackName() { return trackName; }
    public boolean isLoop() { return loop; }

    // Setters
    public void setLoop(boolean loop) { this.loop = loop; }
}
