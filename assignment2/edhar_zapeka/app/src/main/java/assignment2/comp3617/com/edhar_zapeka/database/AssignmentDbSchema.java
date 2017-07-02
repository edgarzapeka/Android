package assignment2.comp3617.com.edhar_zapeka.database;

/**
 * Created by edz on 2017-06-20.
 */

public class AssignmentDbSchema {

    public static final class TaskTable{
        public static final String NAME = "tasks";

        public static final class Cols{
            public static final String ID = "id";
            public static final String TITLE = "title";
            public static final String TEXT = "text";
            public static final String REMINDER = "reminder";
            public static final String PRIORITY = "priority";
            public static final String STATUS = "status";
        }
    }
}
