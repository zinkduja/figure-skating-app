package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class LandingActivity extends AppCompatActivity {

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

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFrag);
            transaction.commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        (new SkaterListAsyncTask()).execute(new String[]{"http://www.isuresults.com/bios/fsbiosladies.htm"});

        // set up bottom navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, LandingFragment.newInstance());
        transaction.commit();

    }

    public void onEventButtonPressed(View view) {
        EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(esFrag, "eventSummary")
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
                   // System.out.println(href + " " + name);

                    // if db is null or skater is not in database, add skater to hashmap
                    if(mDatabase.child("skaters") == null || mDatabase.child("skaters").child(skaterID) == null) {
                        skaterMap.put(skaterID, name);
                        System.out.println("HERE");
                    }
                });

                // update the database
                mDatabase.child("skaters").updateChildren(skaterMap);

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }
    }
}
