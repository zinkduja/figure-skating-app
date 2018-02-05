package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LandingActivity extends AppCompatActivity {

    private TextView mTextMessage;

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

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, LandingFragment.newInstance());
        transaction.commit();
    }

    /**
     * This method opens the skater bio frag when the button is pressed.
     */
    public void onSkaterButtonPressed(View view) {
        SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(sbFrag, "skaterBio")
                // Add this transaction to the back stack
                .addToBackStack(null)
                .replace(R.id.frame_layout, sbFrag)
                .commit();
    }

    /**
     * This method opens the event results frag when the button is pressed.
     */
    public void onEventResultsButtonPressed(View view) {
        EventResultsFragment erFrag = EventResultsFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(erFrag, "skaterBio")
                // Add this transaction to the back stack
                .addToBackStack(null)
                .replace(R.id.frame_layout, erFrag)
                .commit();
    }

    /**
     * This method opens the currently skating frag when the button is pressed.
     */
    public void onCurSkatingButtonPressed(View view) {
        CurrentlySkatingFragment csFrag = CurrentlySkatingFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(CurrentlySkatingFragment.newInstance(), "skaterBio")
                // Add this transaction to the back stack
                .addToBackStack(null)
                .replace(R.id.frame_layout, csFrag)
                .commit();
    }
}
