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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link Fragment} subclass that displays the favorite skaters and
 * events of the current user.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

    private final String TAG = "FavoritesFragment"; // tag for Logcat

    private View.OnClickListener mSkaterListener; // listener to go to skater bio
    private View.OnClickListener mEventListener; // listener to go to event summary

    private View mView; // View for the fragment

    private List<CardView> mSkaterList; // list of favorite skaters as CardViews
    private List<CardView> mEventList; // list of favorite events as CardViews

    private DatabaseReference mDatabase; // reference to Firebase database

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment FavoritesFragment.
     */
    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }

        // Inflate the layout for this fragment and set up the TabHost
        mView = inflater.inflate(R.layout.fragment_favorites, container, false);
        setUpTabHost();

        // set up OnClickListeners
        createListeners();

        // fetch info from database
        mSkaterList = new ArrayList<>();
        mEventList = new ArrayList<>();
        getSkatersFromDB();
        getEventsFromDB();

        return mView;
    }

    /**
     * Sets up the TabHost for this page.
     */
    private void setUpTabHost() {
        TabHost host = mView.findViewById(R.id.tabHost);
        host.setup();

        // Tab 1 - skaters
        TabHost.TabSpec spec = host.newTabSpec("Skaters");
        spec.setContent(R.id.Skaters);
        spec.setIndicator("Skaters");
        host.addTab(spec);

        // Tab 2 - events
        spec = host.newTabSpec("Events");
        spec.setContent(R.id.Events);
        spec.setIndicator("Events");
        host.addTab(spec);
    }

    /**
     * Creates the OnClickListeners.
     * mSkaterListener goes to skater bio when a skater CardViews is clicked
     * mEventListener goes to event summary when an event CardView is clicked
     */
    private void createListeners() {
        mSkaterListener = (View v) -> {
            //pass the name of the skater to the fragment
            SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
            Bundle data = new Bundle();
            LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));
            data.putString("name", ((TextView)(layout.getChildAt(1))).getText().toString());
            sbFrag.setArguments(data);

            // switch to the skater bio page
            getFragmentManager().beginTransaction()
                    .addToBackStack("SKATER_BIO_FRAG")
                    .replace(R.id.frame_layout, sbFrag)
                    .commit();
        };

        mEventListener = (View v) -> {
            //pass the name, startDate, and endDate of the event to the fragment
            EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
            Bundle data = new Bundle();
            LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));

            String title = ((TextView)(layout.getChildAt(0))).getText().toString();
            data.putString("event", title);

            String dates = ((TextView)(layout.getChildAt(1))).getText().toString();
            String start = dates.split(" - ")[0];
            data.putString("startDate", start);
            data.putString("endDate", dates.split(" - ")[1]);

            // get data from the database
            mDatabase.child("competitions").child(title + "--" + start).child("html")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            data.putString("url", dataSnapshot.getValue().toString());
                            esFrag.setArguments(data);

                            // switch to the event summary page
                            getFragmentManager().beginTransaction()
                                    .addToBackStack("EVENT_SUMMARY_FRAG")
                                    .replace(R.id.frame_layout, esFrag)
                                    .commit();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });
        };
    }

    /**
     * Sets a message for if the user has no favorite skaters.
     */
    private void setNoFavoriteSkatersMessage() {
        LinearLayout skatersLayout = mView.findViewById(R.id.skatersLayout);
        TextView msg = new TextView(skatersLayout.getContext());
        msg.setTextAppearance(R.style.baseFont);
        msg.setText("Not following any skaters");

        skatersLayout.addView(msg);
    }

    /**
     * Sets a message for if the user has no favorite events.
     */
    private void setNoFavoriteEventsMessage() {
        LinearLayout eventsLayout = mView.findViewById(R.id.eventsLayout);
        TextView msg = new TextView(eventsLayout.getContext());
        msg.setTextAppearance(R.style.baseFont);
        msg.setText("Not following any events");

        eventsLayout.addView(msg);
    }

    /**
     * Gets the favorite skaters from the database.
     */
    private void getSkatersFromDB() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // get data from the database
            mDatabase.child("favorites").child("skaters").child(email[0]).orderByValue()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // fetch the data
                    if (dataSnapshot.getValue() != null) {
                        (new FetchSkatersAsyncTask()).execute(dataSnapshot);
                    } else {
                        setNoFavoriteSkatersMessage();
                    }
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
     * Gets the favorite events from the database.
     */
    private void getEventsFromDB() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // get data from the database
            mDatabase.child("favorites").child("events").child(email[0]).orderByValue()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // fetch the data
                    if (dataSnapshot.getValue() != null) {
                        (new FetchEventsAsyncTask()).execute(dataSnapshot);
                    } else {
                        setNoFavoriteEventsMessage();
                    }
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
     * Factory method to create and format an ImageView for each skater.
     * @param layout - the enclosing LinearLayout
     * @return - the ImageView generated
     */
    private ImageView createSkaterPic(LinearLayout layout, String name) {
        ImageView temp = new ImageView(layout.getContext());

        temp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        temp.setForegroundGravity(Gravity.LEFT);
        temp.setBaselineAlignBottom(false);

        if (name != null && name.contains("&")) {
            temp.setImageResource(R.drawable.pair_skaters);
        } else {
            temp.setImageResource(R.drawable.single_skater);
        }

        return temp;
    }

    /**
     * Factory method to create and format a TextView for each skater.
     * @param layout - the enclosing LinearLayout
     * @param skaterName - name of the skater
     * @return - the TextView generated
     */
    private TextView createSkaterText(LinearLayout layout, String skaterName) {
        TextView temp = new TextView(layout.getContext());

        temp.setLayoutParams(new LinearLayout.LayoutParams(533, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        temp.setGravity(Gravity.CENTER);
        temp.setPadding(27, 27, 27, 27);
        temp.setText(skaterName);
        temp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        temp.setTextAppearance(R.style.baseFont);

        return temp;
    }

    /**
     * AsyncTask to create CardViews for skaters using info from the database.
     */
    private class FetchSkatersAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                dataSnapshots[0].getChildren().forEach(skater -> {
                    LinearLayout skatersLayout = mView.findViewById(R.id.skatersLayout);

                    // set up CardView
                    CardView cardView = new CardView(skatersLayout.getContext());
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.paleBlue));
                    CardView.LayoutParams params = new CardView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 200, Gravity.CENTER);
                    params.setMargins(5, 0, 5, 30);
                    cardView.setLayoutParams(params);
                    cardView.setRadius(4);

                    // set up LinearLayout
                    LinearLayout layout = new LinearLayout(cardView.getContext());
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setPadding(0, 5, 0, 5);

                    // add image formatting
                    ImageView pic = createSkaterPic(layout, skater.getKey());
                    layout.addView(pic);

                    // add text formatting
                    TextView name = createSkaterText(layout, skater.getKey());
                    layout.addView(name);

                    // set listener, add CardView to list of skaters
                    cardView.addView(layout);
                    cardView.setOnClickListener(mSkaterListener);
                    mSkaterList.add(cardView);
                });
                //Log.i(TAG, "Successful fetch of favorite skaters from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            LinearLayout skatersLayout = mView.findViewById(R.id.skatersLayout);

            // add layouts to page
            mSkaterList.forEach(skatersLayout::addView);

            // add blanks at end (underneath the bottom nav bar)
            TextView textView;
            for (int i=0; i < 3; i++) {
                textView = new TextView(skatersLayout.getContext());
                textView.setText("blank");
                textView.setTextColor(Color.WHITE);
                skatersLayout.addView(textView);
            }
        }
    }

    /**
     * AsyncTask to create CardViews for events using info from the database.
     */
    private class FetchEventsAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                dataSnapshots[0].getChildren().forEach(event -> {
                    LinearLayout eventsLayout = mView.findViewById(R.id.eventsLayout);
                    String[] key = event.getKey().split("--");

                    // create the CardView
                    CardView cardView = createCardView(key[0], key[1], key[2], eventsLayout);

                    // set listener and add CardView to list of events
                    cardView.setOnClickListener(mEventListener);
                    mEventList.add(cardView);
                });

                //Log.i(TAG, "Successful fetch of favorite events from database");

            } catch(Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        /**
         * Factory method to create and format a CardView for each event.
         * @param eTitle - title of event
         * @param start - start date of event
         * @param end - end date of event
         * @param outerLayout - parent layout of the CardView
         * @return - the CardView generated
         */
        private CardView createCardView(String eTitle, String start, String end, LinearLayout outerLayout) {
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
            layout.setGravity(Gravity.CENTER);

            // add text formatting for title
            TextView title = new TextView(layout.getContext());
            title.setText(eTitle);
            title.setTextAppearance(R.style.baseFont);
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(title);

            // add text formatting for dates
            TextView dates = new TextView(layout.getContext());
            dates.setText(String.format("%s - %s", start, end));
            dates.setTextAppearance(R.style.basicFont2);
            dates.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(dates);

            // set listeners and add to main layout
            cardView.addView(layout);
            cardView.setOnClickListener(mEventListener);

            return cardView;
        }

        @Override
        protected void onPostExecute(Void param) {
            LinearLayout eventsLayout = mView.findViewById(R.id.eventsLayout);

            // add layouts to page
            mEventList.forEach(eventsLayout::addView);

            // add blanks at end (underneath the bottom nav bar)
            TextView textView;
            for (int i=0; i < 3; i++) {
                textView = new TextView(eventsLayout.getContext());
                textView.setText("blank");
                textView.setTextColor(Color.WHITE);
                eventsLayout.addView(textView);
            }
        }
    }
}
