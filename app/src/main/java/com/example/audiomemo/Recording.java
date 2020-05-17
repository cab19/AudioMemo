package com.example.audiomemo;

public class Recording {
    // member variables to store recording data
    private String strFilename;
    private String strDescription;

    // Constructor
    public Recording(String filename, String description) {
        // assign passed in values to member variables
        strFilename = filename;
        strDescription = description;
    }

    //Getter & Setter methods

    public String getFilename() {
        return strFilename;
    }

    public void setFilename(String strFilename) {
        this.strFilename = strFilename;
    }

    public String getDescription() {
        return strDescription;
    }

    public void setDescription(String strDescription) {
        this.strDescription = strDescription;
    }
}
