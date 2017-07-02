package assignment2.comp3617.com.edhar_zapeka;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import assignment2.comp3617.com.edhar_zapeka.data.Task;
import assignment2.comp3617.com.edhar_zapeka.database.TaskDao;
import assignment2.comp3617.com.edhar_zapeka.util.SortTasks;

/**
 * Created by edz on 2017-06-19.
 */

public class TasksListFragment extends Fragment {

    private TasksAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TaskDao mTaskDao;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskDao = new TaskDao(getContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasks_list, container, false);

        List<Task> testTasks = mTaskDao.getTasks();
        SortTasks sortTasks = new SortTasks(new ArrayList<>(testTasks));

        mAdapter = new TasksAdapter(sortTasks.sortTasks());
        mRecyclerView = (RecyclerView) v.findViewById(R.id.tasks_list);
        LinearLayoutManager lm = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.display_tasks_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_menu:
                this.getFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddTaskFragment()).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                return true;
            case R.id.delete_menu:
                mAdapter.mDeleteMode = !mAdapter.mDeleteMode;
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.settings_add_menu:
                this.getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TasksAdapter extends RecyclerView.Adapter<TasksViewHolder>{

        List<Task> mTasks;
        public boolean mDeleteMode = false;

        private TasksAdapter(List<Task> tasks){
            mTasks = tasks;
        }

        @Override
        public TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_list_row, parent, false);

            return new TasksViewHolder(v);
        }

        @Override
        public void onBindViewHolder(TasksViewHolder holder, final int position) {
            if (mDeleteMode){
                holder.mDeleteButton.setVisibility(View.VISIBLE);
                holder.mDeleteButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.delete_anim));
            } else{
                holder.mDeleteButton.setVisibility(View.INVISIBLE);
            }

            if (mPreferences.getBoolean(getResources().getString(R.string.task_priority_key_settings), false)){
                switch (mTasks.get(position).getStatus()){
                    case "In Progress":
                        holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.highPriority));
                        break;
                    case "In Design":
                        holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.mediumPriority));
                        break;
                    case "Done":
                        holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.lowPriority));
                        break;
                    default:
                        break;
                }
            }

            holder.mTitle.setText(String.format(getResources().getString(R.string.title_cardview_text), mTasks.get(position).getTitle()));
            holder.mText.setText(String.format(getResources().getString(R.string.description_cardview_text), ((mTasks.get(position).getText().length() > 129) ? mTasks.get(position).getText().substring(0, 129) + "..." : mTasks.get(position).getText())));
            holder.mDate.setText(String.format(getResources().getString(R.string.date_cardview_text), new SimpleDateFormat("MMMM d yyyy").format(mTasks.get(position).getReminder())));
            holder.mStatus.setText(String.format(getResources().getString(R.string.status_cardview_text), mTasks.get(position).getStatus()));
            holder.mPriority.setText(String.format(getResources().getString(R.string.priority_cardview_text), mTasks.get(position).getPriority()));
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    Fragment f = EditTaskFragment.getInstance(mTasks.get(position).getId());
                    fm.beginTransaction().replace(R.id.fragment_container, f).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                }
            });
            holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Deleting Task")
                            .setMessage("Are you sure you want to delete " + mTasks.get(position).getTitle() + " task?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TaskDao taskDao = new TaskDao(getContext());
                                    taskDao.deleteTask(mTasks.get(position).getId());
                                    Toast.makeText(getContext(), "The task " + mTasks.get(position).getTitle() + " was successfully deleted!", Toast.LENGTH_SHORT).show();
                                    mTasks.remove(position);
                                    mAdapter.mDeleteMode = false;
                                    notifyItemRemoved(position);
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
            holder.mShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d yyyy");
                    String shareBody = mTasks.get(position).getText() + "\n" + "Due Date: " + dateFormat.format(mTasks.get(position).getReminder());

                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mTasks.get(position).getTitle());
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share Task #" + position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }

    }

    private class TasksViewHolder extends RecyclerView.ViewHolder{

        private TextView mTitle;
        private TextView mText;
        private TextView mDate;
        private TextView mPriority;
        private TextView mStatus;
        private ImageButton mDeleteButton;
        private ImageButton mShareButton;
        private CardView mCardView;

        private TasksViewHolder(View v){
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mTitle = (TextView) v.findViewById(R.id.title_text);
            mText = (TextView) v.findViewById(R.id.text_text);
            mDate = (TextView) v.findViewById(R.id.text_date);
            mPriority = (TextView) v.findViewById(R.id.text_priority);
            mStatus = (TextView) v.findViewById(R.id.text_status);
            mDeleteButton = (ImageButton) v.findViewById(R.id.delete_button_image);
            mShareButton = (ImageButton) v.findViewById(R.id.ic_share_image);
        }
    }

}
