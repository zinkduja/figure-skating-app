package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import vandy.cs4279.followfigureskating.dbClasses.Skater;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkaterBioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkaterBioFragment extends Fragment {

    private static final String TAG = "SkaterBioFragment";

    private DatabaseReference mDatabase;
    private Skater mSkater;
    private String mSkaterName;
    private String mSkaterDob;
    private String mSkaterHometown;
    private String mSkaterHeight;
    private String mSkaterCoach;
    private String mSkaterChoreo;
    private String mSkaterFormerCoach;

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

        (new ParsePageAsyncTask()).execute(new String[]{"http://www.isuresults.com/bios/isufs00013802.htm"});
        //populate the page with the info
        mSkaterNameView.setText(mSkaterName);
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Skater> {
        @Override
        protected Skater doInBackground(String... strings) {
            //StringBuffer buffer = new StringBuffer();
            Skater newSkater = new Skater();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                // Get document (HTML page) title
                Element name = doc.getElementById("FormView1_person_cnameLabel");
                Element dob = doc.getElementById("FormView1_person_dobLabel");
                Element heightNum = doc.getElementById("FormView1_person_heightLabel");
                String height = heightNum.text();
                Element heightUnit = doc.getElementById("FormView1_Label20");
                height += heightUnit.text();
                Element hometown = doc.getElementById("FormView1_person_htometownLabel");
                Element coach = doc.getElementById("FormView1_person_media_information_coachLabel");
                Element choreo = doc.getElementById("FormView1_person_media_information_choreographerLabel");
                Element former = doc.getElementById("FormView1_person_media_information_former_coachLabel");
                newSkater = new Skater(name.text(), dob.text(), height, hometown.text(), coach.text(), choreo.text(), former.text());

            } catch (Throwable t) {
                t.printStackTrace();
            }
            return newSkater;
        }

        @Override
        protected void onPostExecute(Skater s) {
            mSkaterNameView.setText(s.getmName());
            mDobView.setText(s.getmDob());
            mHometownView.setText(s.getmHometown());
            mHeightView.setText(s.getmHeight());
            mCoachView.setText(s.getmCoach());
            mChoreographerView.setText(s.getmChoreographer());
            mFormerCoachesView.setText(s.getmFormerCoaches());
        }
    }
}
