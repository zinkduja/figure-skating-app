package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventSummaryFragment extends Fragment {

    private final String TAG = "EventSummaryFragment";
    private final String RESULTS = "Results";
    private final String CUR_SKATE = "Currently Skating";
    private final String DASHES = "---------";

    private View.OnClickListener mListener;
    private View mView;

    private String mEvent;
    private String mTime;
    private TableLayout mTable;
    private List<TableRow> mRows;
    private boolean isPrevColored; //used to color rows

    private DatabaseReference mDatabase;

    public EventSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventSummaryFragment.
     */
    public static EventSummaryFragment newInstance() {
        return new EventSummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRows = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_event_summary, container, false);
        mEvent = getArguments().getString("event");
        createListener();

        // set event title
        TextView title = (TextView) mView.findViewById(R.id.eventTitle);
        title.setText(mEvent);

        // fill in the table
        mRows.clear();
        (new CreateTableAsyncTask()).execute();

        // set up following icon
        setUpFollowingIcon();

        return mView;
    }

    //TODO - change how to determine results or cur skating
    private void createListener() {
        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventResultsFragment erFrag = EventResultsFragment.newInstance();
                Bundle data = new Bundle();
                data.putString("event", mEvent);
                String type = ((TextView) v).getText().toString();
                erFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .addToBackStack("EVENT_RESULTS_FRAG")
                        .replace(R.id.frame_layout, erFrag)
                        .commit();
            }
        };
    }

    /**
     * Set up the follow icon for a skater.
     */
    private void setUpFollowingIcon () {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MaterialFavoriteButton followButton =
                (MaterialFavoriteButton) mView.findViewById(R.id.followEventButton);

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            mDatabase.child("favorites").child("events").child(email[0])
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // set up button
                            if(dataSnapshot.exists()) {
                                // check if event is a favorite
                                boolean fav = false;
                                for(DataSnapshot child : dataSnapshot.getChildren()) {
                                    if(child.getKey().equals(mEvent)) {
                                        fav = true;
                                    }
                                }

                                followButton.setFavorite(fav);
                            }

                            // set up listener for button
                            followButton.setOnFavoriteChangeListener(
                                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                                        @Override
                                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                            if (favorite) {
                                                addFavorite();
                                            } else {
                                                removeFavorite();
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });


        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Add current event to favorites for current user.
     */
    private void addFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");
            mDatabase.child("favorites").child("events").child(email[0]).child(mEvent).setValue(true);
        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Remove current skater from favorites for current user.
     */
    private void removeFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");
            mDatabase.child("favorites")
                    .child("events")
                    .child(email[0])
                    .child(mEvent)
                    .removeValue();
        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Create the time table for the event.
     * @throws IOException
     */
    private void createTable() throws IOException {
        //TODO - change url based on event
        Document doc = Jsoup.connect("http://www.isuresults.com/results/season1718/owg2018/").get();
        mTable = mView.findViewById(R.id.eventTable);

        // get the time setting (should be only 1 element)
        Elements caption = doc.getElementsByClass("caption5");
        mTime = caption.get(0).text();

        // get the 'Time Schedule' table
        Elements webTables = doc.getElementsByTag("table");
        Element table = webTables.get(5).child(0);
        table.child(0).remove();

        createTitleRow();
        table.children().forEach(row -> {
            // date, time, category, segment
            String date = row.child(0).text();
            String time = row.child(1).text();
            String category = row.child(2).text();
            String segment = row.child(3).text();
            createTableRow(date, time, category, segment);
        });

        //insert three blank rows (because of the bottom nav bar)
        for(int i=0; i < 3; i++) {
            TableRow blank = new TableRow(mTable.getContext());
            TextView empty = new TextView(blank.getContext());
            blank.addView(empty);
            mRows.add(blank);
        }
    }

    /**
     * Create the title row for the summary table.
     */
    private void createTitleRow() {
        TableRow row = new TableRow(mTable.getContext());

        TextView textView = new TextView(row.getContext());
        textView.setText("Date");
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        textView = new TextView(row.getContext());
        textView.setText("Time");
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        textView = new TextView(row.getContext());
        textView.setText("Category");
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        textView = new TextView(row.getContext());
        textView.setText("Segment");
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        mRows.add(row);
        isPrevColored = true;
    }

    /**
     * Create a row for the summary table.
     * @param date - String for the date
     * @param time - String for the time
     * @param category - String for the category
     * @param segment - String for the segment
     */
    private void createTableRow(String date, String time, String category, String segment) {
        TableRow row = new TableRow(mTable.getContext());
        if (!date.isEmpty()) { // color rows with new date
            isPrevColored = !isPrevColored;
        }
        if(!isPrevColored) {
            row.setBackgroundColor(getResources().getColor(R.color.paleBlue));
        }

        //date view
        TextView textView = new TextView(row.getContext());
        textView.setText(date);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // time view
        textView = new TextView(row.getContext());
        textView.setText(time.isEmpty() ? DASHES : time.substring(0,5));
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // category view
        textView = new TextView(row.getContext());
        textView.setText(category.isEmpty() ? (DASHES+DASHES+DASHES) : category);
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // segment view
        textView = new TextView(row.getContext());
        if(!segment.isEmpty()) {
            textView.setOnClickListener(mListener);
        }
        textView.setText(segment.isEmpty() ? (DASHES+DASHES) : segment);
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        mRows.add(row);
    }

    private class CreateTableAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // set up the table
            try{
                createTable();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            ((TextView)(mView.findViewById(R.id.timeHolder))).setText(mTime);

            mRows.forEach(row -> {
                mTable.addView(row);
            });
        }
    }
}
