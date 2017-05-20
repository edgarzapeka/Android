package com.comp3617.assignment1;

import java.util.List;
import java.util.UUID;

/**
 * Created by Edhar Zapeka on 2017-05-18.
 */

public class Question {

    private UUID mId;
    private String mText;
    private String mRightAnswer;
    private List<String> mAnswers;
    private int mImageId;

    public Question(String text, String rightAnswer, List<String> wrongAnswers, int imageId){
        mId = UUID.randomUUID();
        mText = text;
        mRightAnswer = rightAnswer;
        mAnswers = wrongAnswers;
        mImageId = imageId;
    }

    public UUID getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public String getRightAnswer() {
        return mRightAnswer;
    }

    public List<String> getAnswers() {
        return mAnswers;
    }

    public int getImageId(){ return mImageId; }
}
