package assignment2.comp3617.com.edhar_zapeka;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import assignment2.comp3617.com.edhar_zapeka.data.Task;
import assignment2.comp3617.com.edhar_zapeka.database.TaskDao;

/**
 * Created by edz on 2017-06-20.
 */

public class EditTaskFragment extends Fragment{

    private static final String TASK_ID = "task_id";
    private static final int REQUEST_DATE_EDIT = 1;
    private static final String DIALOG_DATE = "DialogDateEdit";

    private Task mTask;
    private TaskDao dao;
    private Date mDate;

    private EditText mTitle;
    private EditText mText;
    private Spinner mPriority;
    private Spinner mStatus;
    private Button mDateButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();

        dao = new TaskDao(getContext());
        mTask = dao.getTask(bundle.getInt(TASK_ID));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_task, container, false);


        mTitle = (EditText) v.findViewById(R.id.title_text_edit_fragment);
        mText = (EditText) v.findViewById(R.id.text_text_field_edit_fragment);
        mPriority = (Spinner) v.findViewById(R.id.priority_spinner_edit);
        mStatus = (Spinner) v.findViewById(R.id.status_spinner_edit);
        mDateButton = (Button) v.findViewById(R.id.due_date_button_edit);

        init();
        addEventHandlers();

        return v;
    }

    private void init(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPriority.setAdapter(adapter);
        mPriority.setSelection(adapter.getPosition(mTask.getPriority()));

        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatus.setAdapter(adapter);
        mStatus.setSelection(adapter.getPosition(mTask.getStatus()));

        mTitle.setText(mTask.getTitle());
        mText.setText(mTask.getText());
        mDate = mTask.getReminder();

        mDateButton.setText(new SimpleDateFormat("MMMM d yyyy").format(mDate));
    }

    private void addEventHandlers(){
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mDate);
                dialog.setTargetFragment(EditTaskFragment.this, REQUEST_DATE_EDIT);
                dialog.show(fm, DIALOG_DATE);
            }
        });
    }

    public static EditTaskFragment getInstance(int id){
        Bundle bundle = new Bundle();
        bundle.putInt(TASK_ID, id);

        EditTaskFragment f = new EditTaskFragment();
        f.setArguments(bundle);

        return f;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_task_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_edit_menu:
                mTask.setTitle(mTitle.getText().toString());
                mTask.setText(mText.getText().toString());
                mTask.setPriority(mPriority.getSelectedItem().toString());
                mTask.setStatus(mStatus.getSelectedItem().toString());
                mTask.setReminder(mDate);

                dao.updateTask(mTask);

                getActivity().onBackPressed();
                Toast.makeText(getContext(), "The task " + mTask.getTitle() + " was successfully updated!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.cancel_edit_menu:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        if (requestCode == REQUEST_DATE_EDIT){
            mDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mDateButton.setText(mDate.toString());
        }
    }

}
