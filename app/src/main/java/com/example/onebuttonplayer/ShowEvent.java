package com.example.onebuttonplayer;

public class ShowEvent {
    private String type; // "start", "stop", "pause", "resume"
    private int trackIndex;
    private String trackName;

    public ShowEvent(String type, int trackIndex, String trackName) {
        this.type = type;
        this.trackIndex = trackIndex;
        this.trackName = trackName;
    }

    // Getters
    public String getType() { return type; }
    public int getTrackIndex() { return trackIndex; }
    public String getTrackName() { return trackName; }
}
