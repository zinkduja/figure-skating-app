package vandy.cs4279.followfigureskating;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private String mSkaterName;

    private TextView mSkaterNameView;
    private TextView mSkaterNationView;
    private TextView mDobView;
    private TextView mHometownView;
    private TextView mHeightView;
    private TextView mCoachView;
    private TextView mChoreographerView;
    private TextView mFormerCoachesView;
    private TextView mShortProgramView;
    private TextView mFreeProgramView;
    private TextView mBestShortView;
    private TextView mBestShortCompView;
    private TextView mBestTopView;
    private TextView mBestTopCompView;

    private ImageView mSkaterPhoto;

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

        //get all necessary Views
        mSkaterNameView = (TextView) rootView.findViewById(R.id.skaterName);
        mSkaterNationView = (TextView) rootView.findViewById(R.id.skaterNation);
        mDobView = (TextView) rootView.findViewById(R.id.dob);
        mHometownView = (TextView) rootView.findViewById(R.id.hometown);
        mHeightView = (TextView) rootView.findViewById(R.id.height);
        mCoachView = (TextView) rootView.findViewById(R.id.coach);
        mChoreographerView = (TextView) rootView.findViewById(R.id.choreographer);
        mFormerCoachesView = (TextView) rootView.findViewById(R.id.formerCoaches);
        mShortProgramView = (TextView) rootView.findViewById(R.id.shortprogram);
        mFreeProgramView = (TextView) rootView.findViewById(R.id.freeprogram);
        mBestShortView = (TextView) rootView.findViewById(R.id.bestShort);
        mBestShortCompView = (TextView) rootView.findViewById(R.id.bestShortComp);
        mBestTopView = (TextView) rootView.findViewById(R.id.bestTop);
        mBestTopCompView = (TextView) rootView.findViewById(R.id.bestTopComp);

        mSkaterPhoto = (ImageView) rootView.findViewById(R.id.skaterPhoto);

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

        (new ParsePageAsyncTask()).execute(new String[]{"http://www.isuresults.com/bios/isufs00000005.htm"});

        //check if data is different from database
        //make sure to check if database contains null (first time running app)

        //if needed, update the database:
        //Skater skater = new Skater(mSkaterName, ...);
        //mDatabase.child("skaterBios").child(skaterIsuID).setValue(skater);
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Skater> {
        Element image;

        @Override
        protected Skater doInBackground(String... strings) {
            //StringBuffer buffer = new StringBuffer();
            Skater newSkater = new Skater();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                // Get info from webpage
                System.out.println(doc);
                Element dob = doc.getElementById("FormView1_person_dobLabel");
                Element heightNum = doc.getElementById("FormView1_person_heightLabel");
                String height = heightNum.text();
                Element heightUnit = doc.getElementById("FormView1_Label20");
                height += heightUnit.text();
                Element hometown = doc.getElementById("FormView1_person_htometownLabel");
                Element coach = doc.getElementById("FormView1_person_media_information_coachLabel");
                Element choreo = doc.getElementById("FormView1_person_media_information_choreographerLabel");
                Element former = doc.getElementById("FormView1_person_media_information_former_coachLabel");
                Element nation = doc.getElementById("FormView1_person_nationLabel");
                Element shortProgram = doc.getElementById("FormView1_Label3");
                Element freeProgram = doc.getElementById("FormView1_Label4");
                Element bestShortComp = doc.getElementById("FormView1_GridView3_ctl03_HyperLink1");
                Element bestShort = doc.getElementById("FormView1_GridView3");
                System.out.println(bestShort);
                Element bestTopComp = doc.getElementById("FormView1_GridView3_ctl02_HyperLink1");
                Element bestTop = doc.getElementById("FormView1_GridView3");

                newSkater = new Skater(mSkaterName, dob.text(), height, hometown.text(),
                        coach.text(), choreo.text(), former.text(), nation.text(), shortProgram.text(),
                        freeProgram.text(), bestTop.text(), bestTopComp.text(), bestShort.text(),
                        bestShortComp.text());

                /*//get the image of the skater from wikipedia
                Document wikiDoc = Jsoup.connect().get();
                Elements info = wikiDoc.getElementsByClass("infobox vcard");
                for(Element e : info) {
                    if(e.hasClass("image")) {
                        Uri pic = Uri.parse(e.text());
                        mSkaterPhoto.setImageURI(pic);
                        break;
                    }
                }*/

            } catch (Throwable t) {
                t.printStackTrace();
            }

            return newSkater;
        }

        @Override
        protected void onPostExecute(Skater s) {
            mSkaterNameView.setText(mSkaterName);
            mSkaterNationView.setText(s.getmNation());
            mDobView.setText(s.getmDob());
            mHometownView.setText(s.getmHometown());
            mHeightView.setText(s.getmHeight());
            mCoachView.setText(s.getmCoach());
            mChoreographerView.setText(s.getmChoreographer());
            mFormerCoachesView.setText(s.getmFormerCoaches());
            mShortProgramView.setText(s.getmShortProgram());
            mFreeProgramView.setText(s.getmFreeProgram());
            //mBestTopView.setText(s.getmBestTop());
            mBestTopCompView.setText(s.getmBestTopComp());
            //mBestShortView.setText(s.getmBestShort());
            mBestShortCompView.setText(s.getmBestShortComp());
        }
    }
}
