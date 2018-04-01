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
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

    private final String TAG = "FavoritesFragment";

    private View.OnClickListener mSkaterListener;
    private View.OnClickListener mEventListener;
    private View mView;
    private List<CardView> mSkaterList;
    private List<CardView> mEventList;

    private DatabaseReference mDatabase;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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
        if(container != null) {
            container.removeAllViews();
        }

        // Inflate the layout for this fragment
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
     * Set up the TabHost for this page.
     */
    private void setUpTabHost() {
        TabHost host = (TabHost)mView.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Skaters");
        spec.setContent(R.id.Skaters);
        spec.setIndicator("Skaters");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Events");
        spec.setContent(R.id.Events);
        spec.setIndicator("Events");
        host.addTab(spec);
    }

    private void createListeners() {
        mSkaterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the skater to the fragment
                SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
                Bundle data = new Bundle();
                LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));
                data.putString("name", ((TextView)(layout.getChildAt(1))).getText().toString());
                sbFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .addToBackStack("SKATER_BIO_FRAG")
                        .replace(R.id.frame_layout, sbFrag)
                        .commit();
            }
        };

        mEventListener = new View.OnClickListener() {
            @Override //TODO
            public void onClick(View v) {
                //pass the name of the event to the fragment
                SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
                Bundle data = new Bundle();
                LinearLayout layout = (LinearLayout)(((CardView) v).getChildAt(0));
                data.putString("name", ((TextView)(layout.getChildAt(1))).getText().toString());
                sbFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .addToBackStack("SKATER_BIO_FRAG")
                        .replace(R.id.frame_layout, sbFrag)
                        .commit();
            }
        };
    }

    /**
     * Sets a message for if the user has no favorite skaters.
     */
    private void setNoFavoriteSkatersMessage() {
        LinearLayout skatersLayout = (LinearLayout) mView.findViewById(R.id.skatersLayout);
        TextView msg = new TextView(skatersLayout.getContext());
        msg.setTextAppearance(R.style.baseFont);
        msg.setText("Not following any skaters");

        skatersLayout.addView(msg);
    }

    /**
     * Sets a message for if the user has no favorite events.
     */
    private void setNoFavoriteEventsMessage() {
        LinearLayout eventsLayout = (LinearLayout) mView.findViewById(R.id.eventsLayout);
        TextView msg = new TextView(eventsLayout.getContext());
        msg.setTextAppearance(R.style.baseFont);
        msg.setText("Not following any events");

        eventsLayout.addView(msg);
    }

    /**
     * Get the favorite skaters from the database.
     */
    private void getSkatersFromDB() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            mDatabase.child("favorites").child("skaters").child(email[0]).orderByValue()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // fetch the data
                    if(dataSnapshot.getValue() != null) {
                        (new FetchSkatersAsyncTask()).execute(dataSnapshot);
                    } else {
                        setNoFavoriteSkatersMessage();
                    }
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
     * Get the favorite events from the database.
     */
    private void getEventsFromDB() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            mDatabase.child("favorites").child("events").child(email[0]).orderByValue()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // fetch the data
                    if(dataSnapshot.getValue() != null) {
                        (new FetchEventsAsyncTask()).execute(dataSnapshot);
                    } else {
                        setNoFavoriteEventsMessage();
                    }
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
     * Factory method to create and format an ImageView for each skater.
     * @param layout - the enclosing LinearLayout
     * @return - the ImageView generated
     */
    private ImageView createSkaterPic(LinearLayout layout) {
        ImageView temp = new ImageView(layout.getContext());

        temp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        temp.setForegroundGravity(Gravity.LEFT);
        temp.setBaselineAlignBottom(false);
        temp.setImageResource(R.mipmap.ic_launcher); //TODO

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
     * Factory method to create and format a TextView for each event.
     * @param layout - the enclosing LinearLayout
     * @param eventTitle - title of the event
     * @return - the TextView generated
     */
    private TextView createEventText(CardView layout, String eventTitle) {
        TextView temp = new TextView(layout.getContext());

        temp.setGravity(Gravity.CENTER);
        temp.setPadding(27, 27, 27, 27);
        temp.setText(eventTitle);
        temp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        temp.setTextAppearance(R.style.baseFont);

        return temp;
    }

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
                    ImageView pic = createSkaterPic(layout);
                    layout.addView(pic);

                    // add text formatting
                    TextView name = createSkaterText(layout, skater.getKey());
                    layout.addView(name);

                    cardView.addView(layout);
                    cardView.setOnClickListener(mSkaterListener);
                    mSkaterList.add(cardView);
                });
                Log.w(TAG, "Successful fetch of favorite skaters from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            LinearLayout skatersLayout = (LinearLayout) mView.findViewById(R.id.skatersLayout);

            // add layouts to page
            mSkaterList.forEach(skatersLayout::addView);
            // add blanks at end (underneath the bottom nav bar)
            TextView textView;
            for(int i=0; i < 3; i++) {
                textView = new TextView(skatersLayout.getContext());
                textView.setText("blank");
                textView.setTextColor(Color.WHITE);
                skatersLayout.addView(textView);
            }
        }
    }

    private class FetchEventsAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                dataSnapshots[0].getChildren().forEach(event -> {
                    LinearLayout eventsLayout = mView.findViewById(R.id.eventsLayout);

                    // set up CardView
                    CardView cardView = new CardView(eventsLayout.getContext());
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.paleBlue));
                    CardView.LayoutParams params = new CardView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 200, Gravity.CENTER);
                    params.setMargins(5, 0, 5, 30);
                    cardView.setLayoutParams(params);
                    cardView.setRadius(4);

                    // add text formatting
                    TextView title = createEventText(cardView, event.getKey());
                    cardView.addView(title);

                    cardView.setOnClickListener(mEventListener);
                    mEventList.add(cardView);
                });
                Log.w(TAG, "Successful fetch of favorite skaters from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            LinearLayout eventsLayout = (LinearLayout) mView.findViewById(R.id.eventsLayout);

            // add layouts to page
            mEventList.forEach(eventsLayout::addView);
            // add blanks at end (underneath the bottom nav bar)
            TextView textView;
            for(int i=0; i < 3; i++) {
                textView = new TextView(eventsLayout.getContext());
                textView.setText("blank");
                textView.setTextColor(Color.WHITE);
                eventsLayout.addView(textView);
            }
        }
    }
}
