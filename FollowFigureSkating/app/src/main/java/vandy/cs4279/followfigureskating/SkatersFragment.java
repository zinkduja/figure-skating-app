package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vandy.cs4279.followfigureskating.dbClasses.Skater;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkatersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkatersFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener{

    private final String TAG = "Skaters Fragment";
    private LinearLayout mVertLL;

    private ArrayList<String> mSkaterNameList;
    private ArrayList<LinearLayout> mSkaterViewList;
    private ArrayList<LinearLayout> mCurSkaterViewList;

    private DatabaseReference mDatabase;

    public SkatersFragment() {
        // Required empty public constructor
    }

    /**
     * Returns the fragment.
     * @return - the fragment (this)
     */
    public SkatersFragment getFragment() {
        return this;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkatersFragment.
     */
    public static SkatersFragment newInstance() {
        return new SkatersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and instantiate the ArrayLists
        View rootView = inflater.inflate(R.layout.fragment_skaters, container, false);
        mVertLL = rootView.findViewById(R.id.verticalLL);
        mSkaterViewList = new ArrayList<>();
        mCurSkaterViewList = new ArrayList<>();

        // get skaters and populate page
        getSkatersFromDB();

        // set listener for SearchView
        ((SearchView)(rootView.findViewById(R.id.skaterSearchView))).setOnQueryTextListener(this);

        return rootView;
    }


    /**
     * Factory method to create and format an ImageView for each skater.
     * @param layout - the enclosing LinearLayout
     * @return - the ImageView generated
     */
    public ImageView createSkaterPic(LinearLayout layout) {
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
    public TextView createSkaterText(LinearLayout layout, String skaterName) {
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
     * Fetches all skater names from the database and populates mSkaterNameList.
     */
    public void getSkatersFromDB() {

        mDatabase.child("skaters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // fetch the data
                (new SkatersFragment.FetchSkatersAsyncTask()).execute(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // clear the current views from mCurSkaterViewList
        mCurSkaterViewList.clear();

        // filter the views by newText
        mSkaterViewList.forEach(skaterLayout -> {
            String skaterName = ((TextView) (skaterLayout.getChildAt(1))).getText().toString();
            if (skaterName.toLowerCase().contains(newText.toLowerCase())) {
                mCurSkaterViewList.add(skaterLayout);
            }
        });

        // remove all skaters from the page
        mVertLL.removeAllViewsInLayout();

        // add the filtered views to the page
        mCurSkaterViewList.forEach(skaterLayout -> {
            mVertLL.addView(skaterLayout);
        });

        return false;
    }

    @Override
    public void onClick(View view) {
        //pass the name of the skater to the fragment
        SkaterBioFragment sbFrag = new SkaterBioFragment();
        Bundle data = new Bundle();
        data.putString("name", ((TextView) view).getText().toString());
        sbFrag.setArguments(data);

        //start the fragment
        getFragmentManager()
                .beginTransaction()
                .add(sbFrag, "SKATER_BIO_FRAG")
                .addToBackStack("")
                .replace(R.id.skaterPage, sbFrag)
                .commit();
    }

    private class FetchSkatersAsyncTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(DataSnapshot... dataSnapshots) {
            try {
                mSkaterNameList = new ArrayList<>();
                dataSnapshots[0].getChildren().forEach(dsp -> {
                    mSkaterNameList.add(String.valueOf(dsp.getValue()));
                });

                Log.w(TAG, "Successful fetch of skaters from database");

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mSkaterNameList.forEach(skater -> {
                if(skater.startsWith("A")) {
                    LinearLayout layout = new LinearLayout(mVertLL.getContext());
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setPadding(0, 0, 0, 30);

                    // add image formatting
                    ImageView pic = createSkaterPic(layout);
                    layout.addView(pic);

                    // add text formatting
                    TextView name = createSkaterText(layout, skater);
                    layout.addView(name);

                    // set listeners and add to main layout
                    name.setOnClickListener(getFragment());
                    mVertLL.addView(layout);
                    mSkaterViewList.add(layout);
                }
            });
        }
    }
}
