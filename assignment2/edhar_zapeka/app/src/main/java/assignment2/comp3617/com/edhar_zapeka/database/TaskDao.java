package assignment2.comp3617.com.edhar_zapeka.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import assignment2.comp3617.com.edhar_zapeka.data.Task;

/**
 * Created by edz on 2017-06-20.
 */

public class TaskDao {

    private SQLiteDatabase db;

    public TaskDao(Context context){
        db = new TaskBaseHelper(context).getWritableDatabase();
    }

    public Task getTask(int id){
        TaskCursorWrapper cursor = queryTasks(AssignmentDbSchema.TaskTable.Cols.ID + " = ?", new String[] {(String.valueOf(id))});

        try{
            if (cursor.getCount() == 0){
                return null;
            }

            cursor.moveToFirst();
            return cursor.getTask();
        } finally {
            cursor.close();
        }
    }

    public void addTask(Task task){
        ContentValues values = getContentValues(task);

        db.insert(AssignmentDbSchema.TaskTable.NAME, null, values);
    }

    public void updateTask(Task task){
        String idString = String.valueOf(task.getId());
        ContentValues values = getContentValues(task);

        db.update(AssignmentDbSchema.TaskTable.NAME, values, AssignmentDbSchema.TaskTable.Cols.ID + " = ?", new String[] { idString });
    }

    public void deleteTask(int id){
        db.delete(AssignmentDbSchema.TaskTable.NAME, AssignmentDbSchema.TaskTable.Cols.ID + " = ?", new String[] { String.valueOf(id) });
    }

    public List<Task> getTasks(){
        List<Task> tasks = new ArrayList<>();
        TaskCursorWrapper cursor = queryTasks(null, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                tasks.add(cursor.getTask());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return tasks;
    }

    public TaskCursorWrapper queryTasks(String whereClause, String[] whereArgs){
        Cursor cursor = db.query(AssignmentDbSchema.TaskTable.NAME, null, whereClause, whereArgs, null, null, null);

        return new TaskCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Task task){
        ContentValues values = new ContentValues();
        values.put(AssignmentDbSchema.TaskTable.Cols.TITLE, task.getTitle());
        values.put(AssignmentDbSchema.TaskTable.Cols.TEXT, task.getText());
        values.put(AssignmentDbSchema.TaskTable.Cols.REMINDER, task.getReminder().getTime());
        values.put(AssignmentDbSchema.TaskTable.Cols.PRIORITY, task.getPriority());
        values.put(AssignmentDbSchema.TaskTable.Cols.STATUS, task.getStatus());

        return values;
    }

}
