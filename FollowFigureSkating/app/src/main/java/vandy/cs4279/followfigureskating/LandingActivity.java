package vandy.cs4279.followfigureskating;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandingActivity extends AppCompatActivity {

    private final static String TAG = "Landing Activity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF01798C")));

        // update the skaters from the ISU website
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsladies.htm");
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsmen.htm");
        //TODO - add pairs and ice dance

        // update the events from the ISU website
        (new EventListAsyncTask())
                .execute("https://www.isu.org/figure-skating/figure-skating-events/figure-skating-calendar");

        // set up bottom navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, LandingFragment.newInstance())
                .commit();
    }

    @Override
    public void onBackPressed () {
        Log.d(TAG, "onBackPressed:" + getSupportFragmentManager().getFragments().toString());
        Fragment curFrag = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if(curFrag instanceof LandingFragment || curFrag instanceof SkatersFragment
                || curFrag instanceof FavoritesFragment || curFrag instanceof UserSettingsFragment) {
            // do not go back
        } else {
            super.onBackPressed();
        }
    }

    private class SkaterListAsyncTask extends AsyncTask<String, Void, Void> {

        private DatabaseReference mDatabase;

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
                    if(!skater.child(0).hasClass("noLink")) {
                        String skaterID = skater.child(0).attr("href");
                        skaterID = skaterID.replaceAll("[^0-9]", "");
                        String name = skater.child(0).text();
                        String country = skater.child(2).text();

                        // update the database
                        mDatabase.child("skaters").child(skaterID).setValue(name);
                    }
                });

                Log.i(TAG, "skater database successfully updated");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }

    private class EventListAsyncTask extends AsyncTask<String, Void, Void> {

        private DatabaseReference mDatabase;

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
                    if(!first.text().equals("Cancelled")) {
                        String title = first.attr("title");

                        // get the start/end date and year
                        String[] dates = event.child(1).child(0).text().split("-");
                        String startDate = dates[0].substring(0, dates[0].length()-1);
                        String endDate = dates[1].split(",")[0].substring(1);
                        String year = dates[1].split(",")[1].substring(1);

                        // update the database
                        mDatabase.child("competitions").child(title).child("startDate").setValue(startDate);
                        mDatabase.child("competitions").child(title).child("endDate").setValue(endDate);
                        mDatabase.child("competitions").child(title).child("year").setValue(year);
                    }
                });

                Log.i(TAG, "event database successfully updated");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }
}
