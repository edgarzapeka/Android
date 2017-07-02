package assignment2.comp3617.com.edhar_zapeka.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

import assignment2.comp3617.com.edhar_zapeka.data.Task;

/**
 * Created by edz on 2017-06-20.
 */

public class TaskCursorWrapper extends CursorWrapper {

    public TaskCursorWrapper(Cursor cursor){
        super(cursor);
    }

    public Task getTask(){
        return new Task.Builder()
                .setId(getInt(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.ID)))
                .setTitle(getString(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.TITLE)))
                .setText(getString(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.TEXT)))
                .setReminder(new Date(getLong(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.REMINDER))))
                .setPriority(getString(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.PRIORITY)))
                .setStatus(getString(getColumnIndex(AssignmentDbSchema.TaskTable.Cols.STATUS)))
                .build();
    }

}
