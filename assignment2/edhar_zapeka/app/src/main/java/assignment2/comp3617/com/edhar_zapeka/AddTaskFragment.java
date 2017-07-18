package assignment2.comp3617.com.edhar_zapeka;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import assignment2.comp3617.com.edhar_zapeka.data.Task;
import assignment2.comp3617.com.edhar_zapeka.database.TaskDao;

/**
 * Created by edz on 2017-06-19.
 */

public class AddTaskFragment extends Fragment {

    private static final String DIALOG_DATE = "DialogDateAdd";
    private static final int REQUEST_DATE = 0;
    private static final int PERMISSION_REQUEST_CODE_WRITE_CALENDAR = 2;

    private TaskDao mTaskDao;
    private Date mTaskDate;
    private SharedPreferences mPreferences;

    private TextView mTitle;
    private EditText mText;
    private Spinner mPriority;
    private Spinner mStatus;
    private Button mAddDate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTaskDao = new TaskDao(getContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CODE_WRITE_CALENDAR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_task, container, false);

        mTitle = (TextView) v.findViewById(R.id.title_text_field);
        mText = (EditText) v.findViewById(R.id.text_text_field);
        mPriority = (Spinner) v.findViewById(R.id.priority_spinner_add);
        mStatus = (Spinner) v.findViewById(R.id.status_spinner_add);
        mAddDate = (Button) v.findViewById(R.id.due_date_button);

        init();
        addEventHandlers();

        return v;
    }

    private void init(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPriority.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatus.setAdapter(adapter);
    }

    private void addEventHandlers(){
        mAddDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(new Date());
                dialog.setTargetFragment(AddTaskFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_task_menu, menu);
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_add_menu:
                if (mTitle.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Title is not specified. In order to create a task you have to specify a title", Toast.LENGTH_SHORT).show();
                    return true;
                }
                mTaskDao.addTask(new Task.Builder()
                        .setTitle(mTitle.getText().toString())
                        .setText(mText.getText().toString())
                        .setReminder((mTaskDate == null) ? new Date() : mTaskDate)
                        .setPriority(mPriority.getSelectedItem().toString())
                        .setStatus(mStatus.getSelectedItem().toString())
                        .build());

                Toast.makeText(getContext(), "Task " + mTitle.getText().toString() + " created successfully!", Toast.LENGTH_SHORT).show();

                if ( mPreferences.getBoolean(getResources().getString(R.string.reminder_key_settings), false)){
                    calendarReminder();
                }

                getActivity().onBackPressed();
                return true;
            case R.id.cancel_add_menu:
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

        if (requestCode == REQUEST_DATE){
            mTaskDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            mAddDate.setText(new SimpleDateFormat("MMMM d yyyy").format(mTaskDate));
        }
    }

    private void calendarReminder(){

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CODE_WRITE_CALENDAR);
        } else{
            final ContentValues event = new ContentValues();
            event.put(CalendarContract.Events.CALENDAR_ID, 1);
            event.put(CalendarContract.Events.TITLE, mTitle.getText().toString());
            event.put(CalendarContract.Events.DESCRIPTION, mText.getText().toString());

            event.put(CalendarContract.Events.DTSTART, getDateForCalendar(mTaskDate, mPreferences.getInt(getResources().getString(R.string.time_key_settings), 480)));
            event.put(CalendarContract.Events.DTEND, getDateForCalendar(mTaskDate, mPreferences.getInt(getResources().getString(R.string.time_key_settings), 480)));
            event.put(CalendarContract.Events.ALL_DAY, 0);
            event.put(CalendarContract.Events.HAS_ALARM, 1);

            String timeZone = TimeZone.getDefault().getID();
            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

            Uri baseUri = Uri.parse("content://com.android.calendar/events");

            getContext().getContentResolver().insert(baseUri, event);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Long getDateForCalendar(Date date, int hoursFromPreferences){
        Calendar result = Calendar.getInstance();
        int hours = hoursFromPreferences / 60;
        int minutes = hoursFromPreferences % 60;
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        result.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
        result.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
        result.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
        result.set(Calendar.MINUTE, minutes);
        result.set(Calendar.HOUR, hours);

        return result.getTime().getTime();
    }
}
