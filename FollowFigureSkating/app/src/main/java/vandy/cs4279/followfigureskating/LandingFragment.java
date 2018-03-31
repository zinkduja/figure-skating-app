package vandy.cs4279.followfigureskating;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link LandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LandingFragment extends Fragment {

    private final String TAG = "LandingFragment";

    private View mView;
    private View.OnClickListener mButtonListener;
    private View.OnClickListener mTextListener;

    private List<SkatingEvent> mCurrentEvents;
    private List<SkatingEvent> mUpcomingEvents;
    private List<SkatingEvent> mRecentEvents;

    private DatabaseReference mDatabase;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_landing, container, false);

        // make the OnClickListener
        createListener();

        // make all the CardViews
        getEventsFromDB();
        createBlanks();

        return mView;
    }

    /**
     * Create OnClickListeners for the TextViews.
     */
    private void createListener() {
        mTextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the event to the fragment
                EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
                Bundle data = new Bundle();
                LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));
                data.putString("event", ((TextView)(layout.getChildAt(0))).getText().toString());
                esFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .addToBackStack("EVENT_SUMMARY_FRAG")
                        .replace(R.id.frame_layout, esFrag)
                        .commit();
            }
        };
    }

    //add blanks at end (underneath the bottom nav bar)
    private void createBlanks() {
        // add blanks at end (underneath the bottom nav bar)
        LinearLayout layout = mView.findViewById(R.id.landingLayout);
        TextView textView;
        for(int i=0; i < 3; i++) {
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
        mDatabase.child("competitions").orderByChild("startDate")
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

    private class FetchEventsAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                mCurrentEvents = new ArrayList<>();
                mUpcomingEvents = new ArrayList<>();
                mRecentEvents = new ArrayList<>();

                // fetch all the events' basic data
                dataSnapshots[0].getChildren().forEach(event -> {
                    String title = event.getKey();
                    String startDate = "", endDate = "", year = "";

                    for(DataSnapshot child : event.getChildren()) {
                        String key = child.getKey();

                        if(key.equals("startDate")) {
                            startDate = child.getValue().toString();
                        } else if (key.equals("endDate")) {
                            endDate = child.getValue().toString();
                        } else if (key.equals("year")) {
                            year = child.getValue().toString();
                        }
                    }

                    SkatingEvent skatingEvent = new SkatingEvent(title, startDate, endDate, year);

                    try{
                        // check the start and end dates
                        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
                        String completeStart = startDate + " " + year;
                        String completeEnd = endDate + " " + year;
                        Date start = formatter.parse(completeStart);
                        Date end = formatter.parse(completeEnd);
                        Date today = new Date();

                        // add to appropriate list
                        if(end.before(today)) {
                            mRecentEvents.add(skatingEvent);
                        } else if (start.after(today)) {
                            mUpcomingEvents.add(skatingEvent);
                        } else {
                            mCurrentEvents.add(skatingEvent);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });

                Log.i(TAG, "Successful fetch of events from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // fill the page in with the events
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
        }

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

            // add text formatting
            TextView title = new TextView(layout.getContext());
            title.setText(event.getTitle());
            title.setTextAppearance(R.style.basicFont2Bold);
            layout.addView(title);

            TextView dates = new TextView(layout.getContext());
            dates.setText(String.format("%s - %s", event.getStart(), event.getEnd()));
            dates.setTextAppearance(R.style.basicFont2);
            layout.addView(dates);

            // set listeners and add to main layout
            cardView.addView(layout);
            cardView.setOnClickListener(mTextListener);

            return cardView;
        }
    }
}
