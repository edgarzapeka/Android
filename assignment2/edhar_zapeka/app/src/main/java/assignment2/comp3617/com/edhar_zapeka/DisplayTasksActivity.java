package assignment2.comp3617.com.edhar_zapeka;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

public class DisplayTasksActivity extends AppCompatActivity {

    private FragmentManager mFm;
    private Fragment f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_tasks);

        mFm = getSupportFragmentManager();
        f = mFm.findFragmentById(R.id.fragment_container);

        if (f == null){
            f = new TasksListFragment();
            mFm.beginTransaction().add(R.id.fragment_container, f).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFm.getBackStackEntryCount() == 0){
            super.onBackPressed();
        }

        mFm.popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

}
