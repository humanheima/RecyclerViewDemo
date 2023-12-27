package com.hm.demo.model;

import java.util.Objects;

/**
 * Created by dumingwei on 2017/4/18.
 */
public class TestBean implements Cloneable{

    private String name;
    private String desc;
    private int picture;

    public TestBean(String name, String desc, int picture) {
        this.name = name;
        this.desc = desc;
        this.picture = picture;
    }

    @Override
    public TestBean clone() throws CloneNotSupportedException {
        TestBean bean = null;
        try {
            bean = (TestBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPicture() {
        return picture;
    }

    public void setPicture(int picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestBean testBean = (TestBean) o;
        return picture == testBean.picture && Objects.equals(name, testBean.name) && Objects.equals(
                desc, testBean.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, picture);
    }
}
