package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.ProgressBar;
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
    private View mView;
    private ProgressBar mLoadingBar;

    private ArrayList<String> mSkaterNameList;
    private ArrayList<CardView> mSkaterViewList;
    private ArrayList<CardView> mCurSkaterViewList;

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
        mView = rootView;
        mVertLL = rootView.findViewById(R.id.verticalLL);
        mSkaterViewList = new ArrayList<>();
        mCurSkaterViewList = new ArrayList<>();

        // create and add loading bar
        // mLoadingBar = new ProgressBar(main.getContext());

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
     * Fetches all skater names from the database and populates mSkaterNameList.
     */
    private void getSkatersFromDB() {
        mDatabase.child("skaters").orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
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
        mSkaterViewList.forEach(skaterCard -> {
            LinearLayout layout = (LinearLayout) skaterCard.getChildAt(0);
            String skaterName = ((TextView) (layout.getChildAt(1))).getText().toString();
            if (skaterName.toLowerCase().contains(newText.toLowerCase())) {
                mCurSkaterViewList.add(skaterCard);
            }
        });

        // remove all skaters from the page
        mVertLL.removeAllViewsInLayout();

        // add the filtered views to the page
        mCurSkaterViewList.forEach(skaterCard -> {
            mVertLL.addView(skaterCard);
        });

        return false;
    }

    @Override
    public void onClick(View view) {
        //pass the name of the skater to the fragment
        SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
        Bundle data = new Bundle();
        LinearLayout layout = (LinearLayout)(((CardView) view).getChildAt(0));
        data.putString("name", ((TextView)(layout.getChildAt(1))).getText().toString());
        sbFrag.setArguments(data);

        //start the fragment
        getFragmentManager()
                .beginTransaction()
                .addToBackStack("SKATER_BIO_FRAG")
                .replace(R.id.frame_layout, sbFrag)
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
            // make sure loading bar is hidden
            mView.findViewById(R.id.loadingBar).setVisibility(View.GONE);

            // fill the page in with the skaters
            mSkaterNameList.forEach(skater -> {
                // set up CardView
                CardView cardView = new CardView(mVertLL.getContext());
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
                TextView name = createSkaterText(layout, skater);
                layout.addView(name);

                // set listeners and add to main layout
                cardView.addView(layout);
                cardView.setOnClickListener(getFragment());
                mVertLL.addView(cardView);
                mSkaterViewList.add(cardView);
            });

            // add blanks at end (underneath the bottom nav bar)
            TextView textView;
            for(int i=0; i < 3; i++) {
                textView = new TextView(mVertLL.getContext());
                textView.setText("blank");
                textView.setTextColor(Color.WHITE);
                mVertLL.addView(textView);
            }
        }
    }
}
