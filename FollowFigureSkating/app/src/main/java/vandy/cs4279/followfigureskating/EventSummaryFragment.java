package vandy.cs4279.followfigureskating;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link Fragment} subclass that displays a summary table for a competition.
 * Use the {@link EventSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventSummaryFragment extends Fragment {

    private final String TAG = "EventSummaryFragment"; // tag for Logcat

    private View.OnClickListener mListener; // listener to go to event results
    private View mView; // View for the fragment

    private String mEvent; // title of the current event
    private String mEventStart; // start date of the current event
    private String mEventEnd; // end date of the current event
    private String mTime; // time setting for the current event
    private String mURL; //url for the event

    private TableLayout mTable; // table to hold the data
    private TableLayout mResultsTable; //table to hold the major results table
    private List<TableRow> mRows; // list of table rows
    private List<TableRow> mResultRows; //list of result table rows
    private boolean isPrevColored; // boolean used to color rows

    private DatabaseReference mDatabase; // reference to Firebase database

    public EventSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
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
        mResultRows = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        mRows.clear();
        mResultRows.clear();

        // Inflate the layout for this fragment and get arguments
        mView = inflater.inflate(R.layout.fragment_event_summary, container, false);
        mEvent = getArguments().getString("event");
        mEventStart = getArguments().getString("startDate");
        mEventEnd = getArguments().getString("endDate");
        mURL = getArguments().getString("url");

        // get table views
        mTable = mView.findViewById(R.id.eventTable);
        mResultsTable = mView.findViewById(R.id.resultsTable);

        // set event title
        TextView title = mView.findViewById(R.id.eventTitle);
        title.setText(mEvent);

        if(mURL.equals("")) { // no url for the event
            TableRow row = new TableRow(mTable.getContext());
            TextView text = new TextView(row.getContext());
            text.setText(R.string.noScheduleText);
            text.setTextAppearance(R.style.mediumBaseFont);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(text);
            mTable.addView(row);
            TextView timeHolder = mView.findViewById(R.id.timeHolder);
            timeHolder.setVisibility(View.GONE);
            MaterialFavoriteButton button = mView.findViewById(R.id.followEventButton);
            button.setVisibility(View.GONE);
        }
        else {
            // create the OnClickListener
            createListener();

            // fill in the table
            (new CreateTableAsyncTask()).execute();

            // set up following icon
            setUpFollowingIcon();
        }

        return mView;
    }

    /**
     * Creates an OnClickListener - goes to the results of a section of
     * the current competition.
     */
    private void createListener() {
        mListener = (View v) -> {
            // pass the event title to the fragment
            EventResultsFragment erFrag = EventResultsFragment.newInstance();
            Bundle data = new Bundle();
            data.putString("event", mEvent);
            data.putString("url", mURL);
            data.putString("html", (String) v.getTag(R.id.html));
            if(v.getTag(R.id.isShort).equals("SP")) {
                data.putBoolean("isShort", true);
            }
            if(v.getTag(R.id.isTeam).equals("Team")) {
                data.putBoolean("isTeam", true);
            }
            if(v.getTag(R.id.isOverall).equals("Yes")) {
                data.putBoolean("isOverall", true);
            }
            erFrag.setArguments(data);

            // switch to the event results page
            getFragmentManager().beginTransaction()
                    .addToBackStack("EVENT_RESULTS_FRAG")
                    .replace(R.id.frame_layout, erFrag)
                    .commit();
        };
    }

    /**
     * Sets up the follow icon for a skater.
     */
    private void setUpFollowingIcon() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MaterialFavoriteButton followButton = mView.findViewById(R.id.followEventButton);

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // check database to see if current event is a user favorite
            mDatabase.child("favorites").child("events").child(email[0])
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // set up button
                            if (dataSnapshot.exists()) {
                                // check if event is a favorite and set the button
                                boolean fav = false;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (child.getKey().equals((mEvent + "--" + mEventStart + "--" + mEventEnd))) {
                                        fav = true;
                                    }
                                }

                                followButton.setFavorite(fav);
                            }

                            // set up listener for button
                            followButton.setOnFavoriteChangeListener((MaterialFavoriteButton buttonView, boolean favorite) -> {
                                if (favorite) {
                                    addFavorite();
                                } else {
                                    removeFavorite();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });
        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Adds current event to favorites for current user.
     */
    private void addFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // add event to favorites
            String key = mEvent + "--" + mEventStart + "--" + mEventEnd;
            mDatabase.child("favorites").child("events").child(email[0])
                    .child(key).setValue(true);
        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Removes current event from favorites for current user.
     */
    private void removeFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");
            String key = mEvent + "--" + mEventStart + "--" + mEventEnd;

            // remove event from favorites
            mDatabase.child("favorites")
                    .child("events")
                    .child(email[0])
                    .child(key)
                    .removeValue();
        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Creates links to overall results.
     */
    private void createOverallTable() {
        TableRow row1 = new TableRow(mResultsTable.getContext());
        TextView textView = new TextView(row1.getContext());
        textView.setText(R.string.overallResults);
        textView.setGravity(Gravity.CENTER);
        textView.setTextAppearance(R.style.smallBaseFont);
        row1.addView(textView);
        mResultRows.add(row1);

        if(mEvent.contains("Olympics")) {
            row1 = new TableRow(mResultsTable.getContext());
            textView = new TextView(row1.getContext());
            textView.setText(R.string.teamEvent);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(mListener);
            textView.setTag(R.id.html, "TEC001RS");
            textView.setTag(R.id.isOverall, "Yes");
            textView.setTag(R.id.isShort, "no");
            textView.setTag(R.id.isTeam, "Team");
            textView.setTextColor(0xFF000000);
            row1.addView(textView);
            mResultRows.add(row1);
        }

        row1 = new TableRow(mResultsTable.getContext());
        textView= new TextView(row1.getContext());
        textView.setText(R.string.menSingle);
        textView.setOnClickListener(mListener);
        textView.setTag(R.id.html, "CAT001RS");
        textView.setTag(R.id.isOverall, "Yes");
        textView.setTag(R.id.isShort, "no");
        textView.setTag(R.id.isTeam, "no");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0xFF000000);
        row1.addView(textView);
        mResultRows.add(row1);

        row1 = new TableRow(mResultsTable.getContext());
        textView = new TextView(row1.getContext());
        textView.setText(R.string.ladiesSingle);
        textView.setOnClickListener(mListener);
        textView.setTag(R.id.html, "CAT002RS");
        textView.setTag(R.id.isOverall, "Yes");
        textView.setTag(R.id.isShort, "no");
        textView.setTag(R.id.isTeam, "no");
        textView.setTextColor(0xFF000000);
        textView.setGravity(Gravity.CENTER);
        row1.addView(textView);
        mResultRows.add(row1);

        row1 = new TableRow(mResultsTable.getContext());
        textView = new TextView(row1.getContext());
        textView.setText(R.string.pairsSkating);
        textView.setOnClickListener(mListener);
        textView.setTag(R.id.html, "CAT003RS");
        textView.setTag(R.id.isOverall, "Yes");
        textView.setTag(R.id.isShort, "no");
        textView.setTag(R.id.isTeam, "no");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0xFF000000);
        row1.addView(textView);
        mResultRows.add(row1);

        row1 = new TableRow(mResultsTable.getContext());
        textView = new TextView(row1.getContext());
        textView.setText(R.string.icedanceSkating);
        textView.setTextColor(0xFF000000);
        textView.setOnClickListener(mListener);
        textView.setTag(R.id.html, "CAT004RS");
        textView.setTag(R.id.isOverall, "Yes");
        textView.setTag(R.id.isShort, "no");
        textView.setTag(R.id.isTeam, "no");
        textView.setGravity(Gravity.CENTER);
        row1.addView(textView);
        mResultRows.add(row1);

        TableRow blank1 = new TableRow(mResultsTable.getContext());
        TextView empty1 = new TextView(blank1.getContext());
        blank1.addView(empty1);
        mResultRows.add(blank1);
    }

    /**
     * Creates the time table for the event.
     * @throws IOException
     */
    private void createTable() throws IOException {
        // connect to web with jsoup
        Document doc = Jsoup.connect(mURL).get();
        boolean isISUPage = mURL.contains("isuresults");

        // get the time setting (should be only 1 element)
        Elements caption = doc.getElementsByClass("caption5");
        mTime =  caption.size() == 0 ? "(Local Time Zone)" : caption.get(0).text();

        // get the 'Time Schedule' table
        Elements webTables = doc.getElementsByTag("table");
        Element table = isISUPage ? webTables.get(5).child(0) : webTables.get(1).child(1);
        table.child(0).remove();

        // go through the table on the website and create table rows for the local table
        createTitleRow();
        table.children().forEach(row -> {
            // date, time, category, segment
            String time = "", category = "", segment = "";
            String date = row.child(0).text();
            if (isISUPage) {
                time = row.child(1).text();
                category = row.child(2).text();
                segment = row.child(3).text();
            } else {
                if (date.isEmpty()) {
                    time = row.child(1).text();
                    if (time.equals("to follow")) {
                        time = "  -  ";
                    }
                    category = row.child(2).text().split(" - ")[0];
                    segment = row.child(3).child(0).text();
                }
            }

            createTableRow(date, time, category, segment);
        });

        // insert three blank rows (because of the bottom nav bar)
        for(int i=0; i < 3; i++) {
            TableRow blank = new TableRow(mTable.getContext());
            TextView empty = new TextView(blank.getContext());
            blank.addView(empty);
            mRows.add(blank);
        }
    }

    /**
     * Creates the title row for the summary table.
     */
    private void createTitleRow() {
        TableRow row = new TableRow(mTable.getContext());

        // TextView for the date
        TextView textView = new TextView(row.getContext());
        textView.setText(R.string.dateText);
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        // TextView for the time
        textView = new TextView(row.getContext());
        textView.setText(R.string.timeText);
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        // TextView for the category
        textView = new TextView(row.getContext());
        textView.setText(R.string.categoryText);
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        // TextView for the segment
        textView = new TextView(row.getContext());
        textView.setText(R.string.segmentText);
        textView.setTextAppearance(R.style.smallBaseFont);
        row.addView(textView);

        // add the row to the list of rows
        mRows.add(row);
        isPrevColored = true;
    }

    /**
     * Creates a (non-title) row for the summary table.
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

        if (!isPrevColored) {
            row.setBackgroundColor(getResources().getColor(R.color.paleBlue));
        }

        String dashes = "---------"; // dashes used in table formatting

        //date view
        TextView textView = new TextView(row.getContext());
        textView.setText(date);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // time view
        textView = new TextView(row.getContext());
        textView.setText(time.isEmpty() ? dashes : time.substring(0,5));
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // category view
        textView = new TextView(row.getContext());
        textView.setText(category.isEmpty() ? (dashes+dashes+dashes) : category);
        textView.setTextAppearance(R.style.basicFont);
        row.addView(textView);

        // segment view
        textView = new TextView(row.getContext());
        if(!segment.isEmpty()) {
            textView.setOnClickListener(mListener);
        }
        textView.setText(segment.isEmpty() ? (dashes+dashes) : segment);
        textView.setTextAppearance(R.style.basicFont);

        textView.setTag(R.id.isShort, "no");
        textView.setTag(R.id.isTeam, "no");
        textView.setTag(R.id.isOverall, "no");
        if(segment.equals("Short Program") || segment.equals("Short Dance")) {
            textView.setTag(R.id.isShort, "SP");
        }

        if (category.equals("Men Single Skating") || category.equals("Men")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG001" : "SEG002");
        }
        else if (category.equals("Ladies Single Skating") || category.equals("Ladies")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG003" : "SEG004");
        }
        else if (category.equals("Pair Skating") || category.equals("Pairs")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG005" : "SEG006");
        }
        else if (category.equals("Ice Dance")) {
            textView.setTag(R.id.html, segment.equals("Short Dance") ? "SEG007" : "SEG008");
        }
        else if (category.equals("Team Men")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG009" : "SEG010");
            textView.setTag(R.id.isTeam, "Team");
        }
        else if (category.equals("Team Ladies")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG011" : "SEG012");
            textView.setTag(R.id.isTeam, "Team");
        }
        else if (category.equals("Team Pairs")) {
            textView.setTag(R.id.html, segment.equals("Short Program") ? "SEG013" : "SEG014");
            textView.setTag(R.id.isTeam, "Team");
        }
        else if (category.equals("Team Ice Dance")) {
            textView.setTag(R.id.html, segment.equals("Short Dance") ? "SEG015" : "SEG016");
            textView.setTag(R.id.isTeam, "Team");
        } else {
            textView.setTag(R.id.html, "other");
        }

        row.addView(textView);

        // add row to list of rows
        mRows.add(row);
    }

    /**
     * AsyncTask to create the summary table for the fragment. This is used
     * because getting data from the web is necessary and cannot be done
     * on the main UI.
     */
    private class CreateTableAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // set up the table
            try {
                createOverallTable();
                createTable();
                //Log.i(TAG, "Information successfully pulled from ISU website");
            } catch (IOException e) {
                e.printStackTrace();
                //Log.e(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            ((TextView)(mView.findViewById(R.id.timeHolder))).setText(mTime);

            // add all the rows to the table
            mResultRows.forEach(mResultsTable::addView);
            mRows.forEach(mTable::addView);
        }
    }
}
