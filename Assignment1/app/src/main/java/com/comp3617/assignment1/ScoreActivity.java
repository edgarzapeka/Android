package com.comp3617.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ScoreActivity extends AppCompatActivity {

    private TextView mNameTextView;
    private TextView mResultTextView;
    private Button mStartAgain;
    private Button mShareButton;
    private ImageView mImage;

    private int mScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        mImage = (ImageView) findViewById(R.id.congratsImage);
        mImage.setImageResource(R.drawable.congrat);
        mStartAgain = (Button) findViewById(R.id.startAgainButton);
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mResultTextView = (TextView) findViewById(R.id.resultTextView);
        mShareButton = (Button) findViewById(R.id.shareButton);

        Intent intent = getIntent();
        mNameTextView.setText("Good job " + intent.getStringExtra("userName"));
        mScore = getScore((HashMap<String, Boolean>) intent.getSerializableExtra("resultsMap"));
        mResultTextView.setText("Your score: " + mScore);

        addEventHandlers();
    }

    private void addEventHandlers(){
        mStartAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScoreActivity.this, MainActivity.class));
                finish();
            }
        });
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "I've just played in Assignment1 and my score is " + mScore);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    private int getScore(HashMap<String, Boolean> resultSet ){
        int result = 0;
        for(Map.Entry<String, Boolean> entry : resultSet.entrySet()){
            if (entry.getValue()){
                result++;
            }
        }

        return result;
    }

}
