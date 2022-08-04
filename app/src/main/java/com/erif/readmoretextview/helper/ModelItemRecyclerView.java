package com.erif.readmoretextview.helper;

public class ModelItemRecyclerView {

    private int id;
    private String text;
    private boolean collapsed = true;

    public ModelItemRecyclerView() {}

    public ModelItemRecyclerView(int id, String text) {
        this.id = id;
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

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }
}
