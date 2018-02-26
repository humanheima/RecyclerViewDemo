package com.hm.demo.model;

/**
 * Created by dumingwei on 2017/10/10.
 */
public class CheckBoxModel {

    private String description;
    private boolean checked;

    public CheckBoxModel(String description, boolean checked) {
        this.description = description;
        this.checked = checked;
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
}
