package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vandy.cs4279.followfigureskating.dbClasses.SkatingEvent;


/**
 * A {@link Fragment} subclass that displays the home page (list of events).
 * Use the {@link LandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LandingFragment extends Fragment {

    private final String TAG = "LandingFragment"; // tag for Logcat

    private View mView; // View for the fragment

    private View.OnClickListener mTextListener; // listener to go to event summary

    private List<SkatingEvent> mCurrentEvents; // list of current (ongoing) events
    private List<SkatingEvent> mUpcomingEvents; // list of upcoming events
    private List<SkatingEvent> mRecentEvents; // list of recent (past) events

    private SimpleDateFormat mFormatter; // date formatter

    private DatabaseReference mDatabase; // reference to Firebase database

    public LandingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LandingFragment.
     */
    public static LandingFragment newInstance() {
        return new LandingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCurrentEvents = new ArrayList<>();
        mUpcomingEvents = new ArrayList<>();
        mRecentEvents = new ArrayList<>();
        mFormatter = new SimpleDateFormat("MMM dd yyyy");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_landing, container, false);

        // make the OnClickListener
        createListener();

        // make sure all lists are empty
        mCurrentEvents.clear();
        mUpcomingEvents.clear();
        mRecentEvents.clear();

        // make all the CardViews
        getEventsFromDB();
        createBlanks();

        return mView;
    }

    /**
     * Creates OnClickListener for the CardViews to go to the event summary page.
     */
    private void createListener() {
        mTextListener = (View v) -> {
            //pass the name, startDate, and endDate of the event to the fragment
            EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
            Bundle data = new Bundle();
            LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));
            data.putString("event", ((TextView)(layout.getChildAt(0))).getText().toString());
            String dates = ((TextView)(layout.getChildAt(1))).getText().toString();
            data.putString("startDate", dates.split(" - ")[0]);
            data.putString("endDate", dates.split(" - ")[1]);
            esFrag.setArguments(data);

            // switch to the event summary page
            getFragmentManager().beginTransaction()
                    .addToBackStack("EVENT_SUMMARY_FRAG")
                    .replace(R.id.frame_layout, esFrag)
                    .commit();
        };
    }

    /**
     * Creates and adds blanks at end (underneath the bottom nav bar)
     */
    private void createBlanks() {
        LinearLayout layout = mView.findViewById(R.id.landingLayout);
        TextView textView;
        for (int i=0; i < 3; i++) {
            textView = new TextView(layout.getContext());
            textView.setText("blank");
            textView.setTextColor(Color.WHITE);
            layout.addView(textView);
        }
    }

    /**
     * Fetches all events from the database.
     */
    private void getEventsFromDB() {
        mDatabase.child("competitions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // fetch the data
                (new FetchEventsAsyncTask()).execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * AsyncTask to fetch event information and populate the page.
     */
    private class FetchEventsAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                // fetch all the events' basic data
                dataSnapshots[0].getChildren().forEach(event -> {
                    String title = event.getKey().split("--")[0]; //remove startDate from title
                    String startDate = "", endDate = "", year = "", location = "";

                    for(DataSnapshot child : event.getChildren()) {
                        String key = child.getKey();

                        if (key.equals("startDate")) {
                            startDate = child.getValue().toString();
                        } else if (key.equals("endDate")) {
                            endDate = child.getValue().toString();
                        } else if (key.equals("year")) {
                            year = child.getValue().toString();
                        } else if (key.equals("location")) {
                            location = child.getValue().toString();
                        }
                    }

                    // create SkatingEvent based on info from database
                    SkatingEvent skatingEvent = new SkatingEvent(title, startDate, endDate, year, location);

                    try{
                        // check the start and end dates
                        String completeStart = startDate + " " + year;
                        String completeEnd = endDate + " " + year;
                        Date start = mFormatter.parse(completeStart);
                        Date end = mFormatter.parse(completeEnd);
                        Date today = new Date();

                        // add to appropriate list
                        if (end.before(today) && mRecentEvents.size() < 3) {
                            mRecentEvents.add(skatingEvent);
                        } else if (start.after(today) && mUpcomingEvents.size() < 3) {
                            mUpcomingEvents.add(skatingEvent);
                        } else if (mCurrentEvents.size() < 3) {
                            mCurrentEvents.add(skatingEvent);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, e.getMessage());
                    }
                });

                Log.i(TAG, "Successful fetch of events from database");

            } catch (Throwable t) {
                Log.e(TAG, t.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // fill the page in with the events
            // if user clicks to another tab before loading, swallow exception
            try {
                // sort by date
                sortEventsByDate(mCurrentEvents);
                sortEventsByDate(mUpcomingEvents);
                sortEventsByDate(mRecentEvents);

                // add events to page
                mCurrentEvents.forEach(event -> {
                    LinearLayout layout = mView.findViewById(R.id.currentEventsLayout);
                    CardView cardView = createCardView(event, layout);
                    layout.addView(cardView);
                });

                mUpcomingEvents.forEach(event -> {
                    LinearLayout layout = mView.findViewById(R.id.upcomingEventsLayout);
                    CardView cardView = createCardView(event, layout);
                    layout.addView(cardView);
                });

                mRecentEvents.forEach(event -> {
                    LinearLayout layout = mView.findViewById(R.id.recentEventsLayout);
                    CardView cardView = createCardView(event, layout);
                    layout.addView(cardView);
                });
            } catch (IllegalStateException e) {
                Log.w(TAG, "User switched to another tab before LandingFragment loaded.");
            }
        }

        /**
         * Factory method to create and format a CardView for each event.
         * @param event - SkaktingEvent to be displayed
         * @param outerLayout - parent layout of CardView
         * @return - the CardView generated
         */
        private CardView createCardView(SkatingEvent event, LinearLayout outerLayout) {
            // set up CardView
            CardView cardView = new CardView(outerLayout.getContext());
            cardView.setCardBackgroundColor(getResources().getColor(R.color.paleBlue));
            CardView.LayoutParams params = new CardView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            params.setMargins(5, 0, 5, 30);
            cardView.setLayoutParams(params);
            cardView.setRadius(4);

            // set up LinearLayout
            LinearLayout layout = new LinearLayout(cardView.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(5, 0, 5, 0);

            // add text formatting for title
            TextView title = new TextView(layout.getContext());
            title.setText(event.getTitle());
            title.setTextAppearance(R.style.mediumBaseFont);
            layout.addView(title);

            // add text formatting for dates
            TextView dates = new TextView(layout.getContext());
            dates.setText(String.format("%s - %s", event.getStart(), event.getEnd()));
            dates.setTextAppearance(R.style.mediumBasicFont);
            layout.addView(dates);

            // add text formatting for location
            TextView loc = new TextView(layout.getContext());
            loc.setText(event.getLocation());
            loc.setTextAppearance(R.style.mediumBasicFont);
            layout.addView(loc);

            // set listeners and add to main layout
            cardView.addView(layout);
            cardView.setOnClickListener(mTextListener);

            return cardView;
        }

        /**
         * Sorts the list of events by their starting date.
         * @param list - list of SkatingEvents to be sorted (needs to be instance variable)
         */
        private void sortEventsByDate(List<SkatingEvent> list) {
            list.sort((SkatingEvent o1, SkatingEvent o2) -> {
                // create date strings
                String date1 = o1.getStart() + " " + o1.getYear();
                String date2 = o2.getStart() + " " + o2.getYear();

                try {
                    // convert strings to dates
                    Date d1 = mFormatter.parse(date1);
                    Date d2 = mFormatter.parse(date2);

                    // sort
                    if (d1.before(d2)) {
                        return -1;
                    } else if (d1.after(d2)) {
                        return 1;
                    } else {
                        return 0;
                    }

                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }

                Log.e(TAG, "sortEventsByDate(): This code should not be reached.");
                return 0;
            });
        }
    }
}
