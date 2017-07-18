package com.comp3617.finalproject.worldofcolor.data;

/**
 * Created by edz on 2017-07-12.
 */

public class CardColor {

    private String mName;
    private int mCode;
    private int mTitleColor;
    private int mBodyColor;
    private int mPopulation;

    public CardColor(){}


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public int getTitleColor() {
        return mTitleColor;
    }

    public void setTitleColor(int titleColor) {
        mTitleColor = titleColor;
    }

    public int getBodyColor() {
        return mBodyColor;
    }

    public void setBodyColor(int bodyColor) {
        mBodyColor = bodyColor;
    }

    public int getPopulation() {
        return mPopulation;
    }

    public void setPopulation(int population) {
        mPopulation = population;
    }
}
