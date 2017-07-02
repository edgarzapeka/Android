package assignment2.comp3617.com.edhar_zapeka.util;

import java.util.ArrayList;
import java.util.List;

import assignment2.comp3617.com.edhar_zapeka.data.Task;

/**
 * Created by edz on 2017-07-01.
 */

public class SortTasks {

    private List<Task> mTasks;

    public SortTasks(ArrayList<Task> tasks){
        mTasks = tasks;
    }

    public List<Task> sortTasks(){

        ArrayList<Task> highPriority = new ArrayList<>();
        ArrayList<Task> mediumPriority = new ArrayList<>();
        ArrayList<Task> lowPriority = new ArrayList<>();

        for(Task t: mTasks){
            switch (t.getStatus()){
                case "In Progress":
                    highPriority.add(t);
                    break;
                case "In Design":
                    mediumPriority.add(t);
                    break;
                case "Done":
                    lowPriority.add(t);
                    break;
                default:
                    break;
            }
        }

        highPriority = sortByPriority(highPriority);
        mediumPriority = sortByPriority(mediumPriority);
        lowPriority = sortByPriority(lowPriority);

        highPriority.addAll(mediumPriority);
        highPriority.addAll(lowPriority);

        return highPriority;
    }

    private ArrayList<Task> sortByPriority(ArrayList<Task> tasks){

        int n = tasks.size();
        Task tmpTask = null;

        for (int i = 0; i < n; i++) {
            for (int j = 1; j < (n - i); j++) {

                if (getPriorityNumber(tasks.get(j-1).getPriority()) > getPriorityNumber(tasks.get(j).getPriority())) {
                    tmpTask = tasks.get(j-1);
                    tasks.set(j-1, tasks.get(j));
                    tasks.set(j, tmpTask);
                }
            }
        }

        return tasks;
    }

    private int getPriorityNumber(String status){
        switch (status){
            case "High":
                return 1;
            case "Medium":
                return 2;
            case "Low":
                return 3;
            default:
                return 0;
        }
    }

}
