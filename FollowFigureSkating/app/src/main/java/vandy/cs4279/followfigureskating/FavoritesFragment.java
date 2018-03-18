package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private View.OnClickListener mListener;
    private View mView;
    private List<LinearLayout> mSkaterList;

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
        mSkaterList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_favorites, container, false);
        setUpTabHost();

        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the skater to the fragment
                SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
                Bundle data = new Bundle();
                data.putString("name", ((TextView) v).getText().toString());
                sbFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .add(sbFrag, "SKATER_BIO_FRAG")
                        .addToBackStack("")
                        .replace(R.id.favoritesPage, sbFrag)
                        .commit();
            }
        };

        getSkatersFromDB();

        //TextView tv = mView.findViewById(R.id.textView1);
        //tv.setOnClickListener(mListener);

        return mView;
    }

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

    private void setNoFavoritesMessage() {
        LinearLayout skaterLayout = (LinearLayout) mView.findViewById(R.id.Skaters);
        TextView msg = new TextView(skaterLayout.getContext());
        msg.setTextAppearance(R.style.baseFont);
        msg.setText("Not following any skaters");

        skaterLayout.addView(msg);
    }

    private void getSkatersFromDB() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            mDatabase.child("favorites").child("skaters").child(email[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // fetch the data
                    if(dataSnapshot.getValue() != null) {
                        (new FavoritesFragment.FetchSkatersAsyncTask()).execute(dataSnapshot);
                    } else {
                        setNoFavoritesMessage();
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

    private class FetchSkatersAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                dataSnapshots[0].getChildren().forEach(skater -> {
                    LinearLayout skatersLayout = mView.findViewById(R.id.Skaters);
                    LinearLayout layout = new LinearLayout(skatersLayout.getContext());
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setPadding(0, 10, 0, 20);

                    // add image formatting
                    ImageView pic = createSkaterPic(layout);
                    layout.addView(pic);

                    // add text formatting
                    TextView name = createSkaterText(layout, skater.getValue().toString());
                    name.setOnClickListener(mListener);
                    layout.addView(name);

                    mSkaterList.add(layout);
                });
                Log.w(TAG, "Successful fetch of favorite skaters from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            LinearLayout skatersLayout = mView.findViewById(R.id.Skaters);

            // add layouts to page
            mSkaterList.forEach(skatersLayout::addView);
        }
    }
}
