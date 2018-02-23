package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkaterBioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkaterBioFragment extends Fragment {

    private static final String TAG = "SkaterBioFragment";

    private DatabaseReference mDatabase;
    private String mSkaterName;

    private TextView mSkaterNameView;
    private TextView mDobView;
    private TextView mHometownView;
    private TextView mHeightView;
    private TextView mCoachView;
    private TextView mChoreographerView;
    private TextView mFormerCoachesView;

    public SkaterBioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkaterBioFragment.
     */
    public static SkaterBioFragment newInstance() {
        return new SkaterBioFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up reference to database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container != null) {
            container.removeAllViews();
        }
        View rootView = inflater.inflate(R.layout.fragment_skater_bio, container, false);

        //get all necessary TextViews
        mSkaterNameView = (TextView) rootView.findViewById(R.id.skaterName);
        mDobView = (TextView) rootView.findViewById(R.id.dob);
        mHometownView = (TextView) rootView.findViewById(R.id.hometown);
        mHeightView = (TextView) rootView.findViewById(R.id.height);
        mCoachView = (TextView) rootView.findViewById(R.id.coach);
        mChoreographerView = (TextView) rootView.findViewById(R.id.choreographer);
        mFormerCoachesView = (TextView) rootView.findViewById(R.id.formerCoaches);

        //get the skater's name that was passed in
        mSkaterName = getArguments().getString("name");

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        populatePage();
    }

    /**
     * Populates the page with information from the database.
     */
    public void populatePage() {
        //TODO - populate page with info
        //get data from webpage here

        //check if data is different from database
        //make sure to check if database contains null (first time running app)

        //if needed, update the database:
        //Skater skater = new Skater(mSkaterName, ...);
        //mDatabase.child("skaters").child(skaterIsuID).setValue(skater);

        //populate the page with the info
        mSkaterNameView.setText(mSkaterName);
    }
}
