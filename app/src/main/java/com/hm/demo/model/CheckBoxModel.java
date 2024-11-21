package com.hm.demo.model;

/**
 * Created by dumingwei on 2017/10/10.
 */
public class CheckBoxModel {

    private String description;
    private boolean checked;
    private int drawableResId;

    public CheckBoxModel(String description, boolean checked) {
        this.description = description;
        this.checked = checked;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public CheckBoxModel(String description, boolean checked, int drawableResId) {
        this.description = description;
        this.checked = checked;
        this.drawableResId = drawableResId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "CheckBoxModel{" +
                "description='" + description + '\'' +
                ", checked=" + checked +
                '}';
    }
}
