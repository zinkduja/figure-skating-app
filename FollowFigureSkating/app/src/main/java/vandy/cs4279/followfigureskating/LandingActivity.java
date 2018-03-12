package vandy.cs4279.followfigureskating;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        // update the skaters from the ISU website
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsladies.htm");
        (new SkaterListAsyncTask()).execute("http://www.isuresults.com/ws/ws/wsmen.htm");
        //TODO - add pairs and ice dance

        // set up bottom navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, LandingFragment.newInstance())
                .commit();
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();

        List list = getSupportFragmentManager().getFragments();
        int len = list.size();

        if (len >= 0) {
            if (list.get(len-1) instanceof SkatersFragment) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, SkatersFragment.newInstance())
                        .commit();
            }

        }



    }

    /**
     * Handles when the user clicks on the Event Button.
     * @param view - current View
     */
    public void onEventButtonPressed(View view) {
        EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(esFrag, "EVENT_SUMMARY_FRAG")
                // Add this transaction to the back stack
                .addToBackStack("")
                .replace(R.id.frame_layout, esFrag)
                .commit();
    }

    private static class SkaterListAsyncTask extends AsyncTask<String, Void, Void> {

        private DatabaseReference mDatabase;

        @Override
        protected Void doInBackground(String... strings) {

            try {
                // set up database and jsoup
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> skaterMap = new HashMap<>();
                Document doc = Jsoup.connect(strings[0]).get();
                Element skaterTable = doc.getElementById("DataList1");

                // get all skater names
                Elements skaters = skaterTable.select("a[href]");
                skaters.forEach(link -> {
                    // get ISU ID and name of each skater
                    String skaterID = link.attr("href");
                    skaterID = skaterID.replaceAll("[^0-9]", "");
                    String name = link.text();

                    // add pair to map
                    if(!(skaterID.equals(""))) {
                        skaterMap.put(skaterID, name);
                    }
                });


                // update the database
                mDatabase.child("skaters").updateChildren(skaterMap);

                Log.w(TAG, "database successfully updated");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }
}
