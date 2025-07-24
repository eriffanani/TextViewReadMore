package com.erif.readmoretextviewdemo.model;

public class ModelItemRecyclerView {

    private int id;

    private String name;

    private int profile;

    private int img;

    private String text;
    private boolean collapsed = true;
    private boolean showImage = false;

    public ModelItemRecyclerView() {}

    public ModelItemRecyclerView(int id, String name, int profile, int img, String text) {
        this.id = id;
        this.name = name;
        this.profile = profile;
        this.img = img;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getProfile() {
        return profile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }
}
