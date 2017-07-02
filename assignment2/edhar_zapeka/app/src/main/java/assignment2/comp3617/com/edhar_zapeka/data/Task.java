package assignment2.comp3617.com.edhar_zapeka.data;

import java.util.Date;

/**
 * Created by edz on 2017-06-19.
 */

public class Task {

    private int mId;
    private String mTitle;
    private String mText;
    private String mPriority;
    private String mStatus;
    private Date mReminder;

    public Task(){}

    private Task(Builder builder){
        mId = builder.mId;
        mTitle = builder.mTitle;
        mText = builder.mText;
        mPriority = builder.mPriority;
        mStatus = builder.mStatus;
        mReminder = builder.mReminder;
    }

    public static class Builder{

        private int mId;
        private String mTitle;
        private String mText;
        private String mPriority;
        private String mStatus;
        private Date mReminder;

        public Builder(){

        }

        public Builder setId(int id){
            mId = id;
            return this;
        }

        public Builder setTitle(String title){
            mTitle = title;
            return this;
        }

        public Builder setText(String text){
            mText = text;
            return this;
        }

        public Builder setReminder(Date date){
            mReminder = date;
            return this;
        }

        public Builder setPriority(String priority){
            mPriority = priority;
            return this;
        }

        public Builder setStatus(String status){
            mStatus = status;
            return this;
        }

        public Task build(){
            return new Task(this);
        }
    }

    public int getId(){ return mId; }

    public String getTitle() {
        return mTitle;
    }

    public String getText() {
        return mText;
    }

    public Date getReminder() {
        return mReminder;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getPriority() {
        return mPriority;
    }

    public void setPriority(String priority) {
        mPriority = priority;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setReminder(Date reminder) {
        mReminder = reminder;
    }
}
