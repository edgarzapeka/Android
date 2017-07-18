package com.comp3617.finalproject.worldofcolor.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by edz on 2017-07-11.
 */

public class Card {

    private String mDbKey;
    private String mAuthorId;
    private String mImageUrl;
    private String mAuthorName;
    private List<CardColor> mColors;
    private List<String> mLikes;

    public List<CardColor> getColors() {
        return mColors;
    }

    public void setColors(List<CardColor> colors) {
        mColors = colors;
    }

    public Card(){
    }

    public Card(String imageUrl, String authorName){
        mImageUrl = imageUrl;
        mAuthorName = authorName;
        mLikes = new ArrayList<String>();
    }

    public String getAuthorId() {
        return mAuthorId;
    }

    public void setAuthorId(String authorId) {
        mAuthorId = authorId;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public List<String> getLikes() {
        return mLikes;
    }

    public String getDbKey() {
        return mDbKey;
    }

    public void setDbKey(String dbKey) {
        mDbKey = dbKey;
    }

    public void setLikes(List<String> likes) {
        mLikes = likes;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getAuthorsName() {
        return mAuthorName;
    }

    public void setAuthorsName(String authorsName) {
        mAuthorName = authorsName;
    }

    public void setColorsFromHashMap(ArrayList<CardColor> map){
        ArrayList<CardColor> realCardColors = new ArrayList<CardColor>();
        //CardColor cardColorTmp = new CardColor();
        Iterator it = map.iterator();
        while (it.hasNext()) {
            CardColor cardColorTmp = new CardColor();
            HashMap<String, String> cardObject = (HashMap<String, String>)it.next();
            Iterator colorProperties = cardObject.entrySet().iterator();
            while (colorProperties.hasNext()){
                Map.Entry pair = (Map.Entry) colorProperties.next();
                switch ((String)pair.getKey()){
                    case "titleColor":
                        cardColorTmp.setTitleColor((int)((long)pair.getValue()));
                        break;
                    case "name":
                        cardColorTmp.setName((String)pair.getValue());
                        break;
                    case "bodyColor":
                        cardColorTmp.setBodyColor((int)((long)pair.getValue()));
                        break;
                    case "population":
                        cardColorTmp.setPopulation((int)((long)pair.getValue()));
                        break;
                    case "code":
                        cardColorTmp.setCode((int)((long)pair.getValue()));
                        break;
                    default:
                        break;
                }
                colorProperties.remove();
            }
            realCardColors.add(cardColorTmp);
            it.remove(); // avoids a ConcurrentModificationException
        }

        mColors = realCardColors;
    }

    public int getTotalPopulation(){
        int totalPopulation = 0;
        for(CardColor cardColor : mColors){
            totalPopulation += cardColor.getPopulation();
        }

        return totalPopulation;
    }
}
