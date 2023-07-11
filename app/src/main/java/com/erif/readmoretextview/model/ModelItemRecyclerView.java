package com.erif.readmoretextview.model;

public class ModelItemRecyclerView {

    private int id;
    private int img;

    private String text;
    private boolean collapsed = true;

    public ModelItemRecyclerView() {}

    public ModelItemRecyclerView(int id, int img, String text) {
        this.id = id;
        this.img = img;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
