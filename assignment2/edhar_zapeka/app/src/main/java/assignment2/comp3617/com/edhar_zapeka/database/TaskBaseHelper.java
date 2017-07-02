package assignment2.comp3617.com.edhar_zapeka.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by edz on 2017-06-20.
 */

public class TaskBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "assignment_two";

    public TaskBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("create table %s (" + //0
                "%s integer primary key autoincrement, " + //1
                "%s, " + //2
                "%s, " + //3
                "%s, " + //4
                "%s, " +  //5
                "%s " + //6
                ")", //
                AssignmentDbSchema.TaskTable.NAME, //0
                AssignmentDbSchema.TaskTable.Cols.ID, //1
                AssignmentDbSchema.TaskTable.Cols.TITLE, //2
                AssignmentDbSchema.TaskTable.Cols.TEXT, //3
                AssignmentDbSchema.TaskTable.Cols.REMINDER, //4
                AssignmentDbSchema.TaskTable.Cols.PRIORITY, //5
                AssignmentDbSchema.TaskTable.Cols.STATUS)); //6
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
