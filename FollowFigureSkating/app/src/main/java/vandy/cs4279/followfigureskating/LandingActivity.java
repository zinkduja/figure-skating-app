package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Activity is the main Activity for the app.  It is the
 * Activity used by all Fragments.  It has the BottomNavigationView
 * that allows the user to easily navigate through the four main
 * Fragments of the app.
 */
public class LandingActivity extends AppCompatActivity {

    private final static String TAG = "Landing Activity"; // tag for the Logcat

    private Handler mHandler; // Handler for the BottomNavigationBat
    private AtomicInteger mProgressStatus; // status of the progress bar
    private ProgressBar mProgressBar; // loading progress bar

    // navigation bar for the four main Fragments
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set up the view for the Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF01798C")));

        // set up the BottomNavigationView
        setUpBottomNav();

        // initialize instance variables
        mHandler = new Handler();
        mProgressStatus = new AtomicInteger();
        mProgressStatus.set(0);
        mProgressBar = findViewById(R.id.progressBar);

        // update the skaters from the ISU website
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsladies.htm");
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsmen.htm");
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wspairs.htm");
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsdance.htm");

        // update the events from the ISU website
        (new EventListAsyncTask())
                .execute("https://www.isu.org/figure-skating/figure-skating-events/figure-skating-calendar");
    }

    /**
     * Sets up the Fragments for the BottomNavigationView
     */
    private void setUpBottomNav() {
        mOnNavigationItemSelectedListener = (@NonNull MenuItem item) -> {
            Fragment selectedFrag = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFrag = LandingFragment.newInstance();
                    break;
                case R.id.navigation_skaters:
                    selectedFrag = SkatersFragment.newInstance();
                    break;
                case R.id.navigation_following:
                    selectedFrag = FavoritesFragment.newInstance();
                    break;
                case R.id.navigation_settings:
                    selectedFrag = UserSettingsFragment.newInstance();
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, selectedFrag)
                    .commit();

            return true;
        };
    }

    /**
     * When the progress bar is complete, this method will be called.
     * Loads the LandingFragment, which is the home page.
     */
    private void loadHomePage(){
        // set up bottom navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, LandingFragment.newInstance())
                .commit();
    }

    @Override
    public void onBackPressed () {
        //Log.d(TAG, "onBackPressed:" + getSupportFragmentManager().getFragments().toString());
        Fragment curFrag = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (curFrag instanceof LandingFragment || curFrag instanceof SkatersFragment
                || curFrag instanceof FavoritesFragment || curFrag instanceof UserSettingsFragment) {
            // do not go back
        } else {
            super.onBackPressed();
        }
    }

    /**
     * AsyncTask to update the skater database. This is used
     * because getting data from the web is necessary and cannot be done
     * on the main UI.
     */
    private class SkaterListAsyncTask extends AsyncTask<String, Void, Void> {

        private DatabaseReference mDatabase; // reference to Firebase database

        @Override
        protected Void doInBackground(String... strings) {

            try {
                // set up skaters child of database and jsoup
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Document doc = Jsoup.connect(strings[0]).get();
                Element skaterTable = doc.getElementById("DataList1");

                // get all skater names
                Elements skaters = skaterTable.getElementsByClass("name");
                skaters.remove(0);

                skaters.forEach(skater -> {
                    // get ISU ID and name of each skater
                    if( !skater.child(0).hasClass("noLink")) {
                        String skaterID = skater.child(0).attr("href");
                        skaterID = skaterID.replaceAll("[^0-9]", "");
                        String name = skater.child(0).text();
                        name = name.replace("/", "&"); // for pairs and ice dance

                        // update the database
                        mDatabase.child("skaters").child(skaterID).setValue(name);
                    }
                });

                //Log.i(TAG, "skater database successfully updated");

                // update the progress bar
                mProgressStatus.incrementAndGet();
                mHandler.post(() -> {
                    mProgressBar.setProgress(mProgressStatus.get());
                });

                if (mProgressStatus.get() == mProgressBar.getMax()) {
                    loadHomePage();
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }

    /**
     * AsyncTask to update the event database. This is used
     * because getting data from the web is necessary and cannot be done
     * on the main UI.
     */
    private class EventListAsyncTask extends AsyncTask<String, Void, Void> {

        private DatabaseReference mDatabase; // reference to Firebase database

        @Override
        protected Void doInBackground(String... strings) {

            try {
                // set up events child of database and jsoup
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Document doc = Jsoup.connect(strings[0]).get();
                Element eventList = doc.getElementById("eventlist");
                Elements events = eventList.getElementsByClass("content");

                // get basic information for events
                events.forEach(event -> {
                    Element first = event.child(0).child(0);

                    // make sure the event wasn't cancelled
                    if (!first.text().equals("Cancelled")) {
                        String title = first.attr("title");
                        title = title.replace("/", " ");
                        String location = event.child(1).child(1).text();
                        location = location.replace(" /", ", ");

                        // get the start/end date and year
                        String[] dates = event.child(1).child(0).text().split("-");
                        String startDate = dates[0].substring(0, dates[0].length()-1);
                        String endDate = dates[1].split(",")[0].substring(1);
                        String year = dates[1].split(",")[1].substring(1);

                        title += "--" + startDate; //add startDate to title for unique key value

                        // update the database
                        mDatabase.child("competitions").child(title).child("startDate").setValue(startDate);
                        mDatabase.child("competitions").child(title).child("endDate").setValue(endDate);
                        mDatabase.child("competitions").child(title).child("year").setValue(year);
                        mDatabase.child("competitions").child(title).child("location").setValue(location);
                    }
                });

                //Log.i(TAG, "event database successfully updated");

                // update the progress bar
                mProgressStatus.incrementAndGet();
                mHandler.post(() -> {
                    mProgressBar.setProgress(mProgressStatus.get());
                });

                if (mProgressStatus.get() == mProgressBar.getMax()) {
                    loadHomePage();
                }

            } catch (Throwable t) {
                //Log.e(TAG, t.getMessage());
                t.printStackTrace();
            }

            return null;
        }
    }
}
