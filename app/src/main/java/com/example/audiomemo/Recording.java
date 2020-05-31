package com.example.audiomemo;

import java.io.Serializable;

public class Recording implements Serializable {
    // member variables to store recording data
    private Integer mID;
    private String mFilename;
    private String mDescription;
    private String mTimeStamp;

    // empty constructor
    public Recording() {

    }

    // Constructor
    public Recording(Integer ID, String filename, String description, String timeStamp) {
        // assign passed in values to member variables
        mID = ID;
        mFilename = filename;
        mDescription = description;
        mTimeStamp = timeStamp;
    }

    //Getter & Setter methods

    public Integer getID() { return mID; }

    public void setID(Integer ID) { this.mID = ID; }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String strFilename) {
        this.mFilename = strFilename;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String strDescription) {
        this.mDescription = strDescription;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(String strTimeStamp) {
        this.mTimeStamp = strTimeStamp;
    }
}
