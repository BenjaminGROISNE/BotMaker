package com.botmaker.library.emulator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlueStacksInstance {
    @JsonProperty("ID")
    private int id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("IsFolder")
    private boolean isFolder;
    @JsonProperty("ParentFolder")
    private int parentFolder;
    @JsonProperty("IsOpen")
    private boolean isOpen;
    @JsonProperty("IsVisible")
    private boolean isVisible;
    @JsonProperty("InstanceName")
    private String instanceName;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public int getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(int parentFolder) {
        this.parentFolder = parentFolder;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
