package com.comp3617.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String CURSOR_KEY = "cursor_key";
    private final String RESULTS_MAP_KEY = "results_map_key";
    private final String USER_NAME_KEY = "user_name_key";

    private String mUserName;
    private List<Question> mQuestionList;
    private int mCursor;
    private HashMap<String, Boolean> mResultsMap;

    private Button mStartButton;
    private EditText mUserNameEditText;
    private Button mNextButton;
    private TextView mQuestionText;
    private RadioGroup mAnswersGroup;
    private RadioButton mFirstAnswerRadioButton;
    private RadioButton mSecondAnswerRadioButton;
    private RadioButton mThirdAnswerRadioButton;
    private ImageView mQuestionImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (savedInstanceState == null){
            initValues();
            mUserNameEditText = (EditText) findViewById(R.id.nameText);
            mStartButton = (Button) findViewById(R.id.startButton);
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserName = mUserNameEditText.getText().toString();
                    startQuestions();
                }
            });
        }else{
            mCursor = savedInstanceState.getInt(CURSOR_KEY);
            mResultsMap = (HashMap) savedInstanceState.get(RESULTS_MAP_KEY);
            mUserName = savedInstanceState.getString(USER_NAME_KEY);
            startQuestions();
        }

    }

    private void initValues(){
        mResultsMap = new HashMap<>();
        mCursor = new Integer(0);
    }

    private void startQuestions(){
        setContentView(R.layout.activity_questions);

        setQuestionList();

        mQuestionImage = (ImageView) findViewById(R.id.questionImage);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mQuestionText = (TextView) findViewById(R.id.questionTextView);
        mAnswersGroup = (RadioGroup) findViewById(R.id.answersGroup);
        mFirstAnswerRadioButton = (RadioButton) findViewById(R.id.firstRadioButton);
        mSecondAnswerRadioButton = (RadioButton) findViewById(R.id.secondRadioButton);
        mThirdAnswerRadioButton = (RadioButton) findViewById(R.id.thirdRadioButton);

        addEventHandlers();

        updateQuestion();
    }

    private void setQuestionList(){
        mQuestionList = new ArrayList<>();

        mQuestionList.add(new Question("Which is the world's highest mountain?", "Mount Everest", new ArrayList<String>(Arrays.asList("Kilimanjaro", "Makalu","Mount Everest")), R.drawable.mountain));
        mQuestionList.add(new Question("Which is the longest river in the U.S.?", "Missouri River", new ArrayList<String>(Arrays.asList("Colorado River", "Yukon River","Missouri River")), R.drawable.us_river));
        mQuestionList.add(new Question("The biggest desert in the world is. . ?", "Sahara", new ArrayList<String>(Arrays.asList("Arabian", "Great Australian","Sahara")), R.drawable.desert));
        mQuestionList.add(new Question("The United Kingdom is comprised of how many countries?", "4", new ArrayList<String>(Arrays.asList("5", "3","4")), R.drawable.great_britain));
        mQuestionList.add(new Question("Which of the following countries do not border France?", "Netherlands", new ArrayList<String>(Arrays.asList("Italy", "Germany","Netherlands")), R.drawable.france));
        mQuestionList.add(new Question("Which is the largest body of water?", "Pacific Ocean", new ArrayList<String>(Arrays.asList("Atlantic Ocean", "Indian Ocean","Pacific Ocean")), R.drawable.ocean));
        mQuestionList.add(new Question("Which of the following created Java programming language?", "James Gosling", new ArrayList<String>(Arrays.asList("James Gosling", "Guido van Rossum","Bjarne Stroustrup")), R.drawable.java));
        mQuestionList.add(new Question("Who is Google CEO?", "Sundar Pichai", new ArrayList<String>(Arrays.asList("Sundar Pichai", "Tim Cook","Satya Nadella")), R.drawable.google_ceo));

        Collections.shuffle(mQuestionList);
    }

    private void updateQuestion(){
        List<String> options = mQuestionList.get(mCursor).getAnswers();
        Collections.shuffle(options);

        mAnswersGroup.clearCheck();

        mQuestionText.setText(mQuestionList.get(mCursor).getText());
        mFirstAnswerRadioButton.setText(options.get(0));
        mSecondAnswerRadioButton.setText(options.get(1));
        mThirdAnswerRadioButton.setText(options.get(2));
        mQuestionImage.setImageResource(mQuestionList.get(mCursor).getImageId());
    }

    private void addEventHandlers(){
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnswersGroup.getCheckedRadioButtonId() != -1){
                    RadioButton answer = (RadioButton) findViewById(mAnswersGroup.getCheckedRadioButtonId());
                    mResultsMap.put(mQuestionList.get(mCursor).getText(), (mQuestionList.get(mCursor).getRightAnswer() == answer.getText())); //Try to simplify
                }
                mCursor++;
                if (mCursor == mQuestionList.size()){
                    finishQuestions();
                } else{
                    updateQuestion();
                }
            }
        });
    }

    private void finishQuestions(){
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("userName", mUserName);
        intent.putExtra("resultsMap", mResultsMap);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(CURSOR_KEY, mCursor);
        outState.putSerializable(RESULTS_MAP_KEY, mResultsMap);
        outState.putString(USER_NAME_KEY, mUserName);

        super.onSaveInstanceState(outState);
    }

}
