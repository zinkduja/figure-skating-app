package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import vandy.cs4279.followfigureskating.dbClasses.Skater;


/**
 * A {@link Fragment} subclass that displays a skater's biography.
 * Use the {@link SkaterBioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkaterBioFragment extends Fragment {

    private static final String TAG = "SkaterBioFragment"; // tag for the Logcat

    private DatabaseReference mDatabase; // reference to Firebase database

    private String mSkaterName; // name of current skater
    private String mSkaterID; // ISU ID of current user

    private ImageView mSkaterImage; // single/pairs image for skater(s)

    private TextView mSkaterNameView; // View for skater name
    private TextView mSkaterNationView; // View for skater's nation
    private TextView mDobView; // View for skater's date of birth
    private TextView mHometownView; // View for skater's hometown
    private TextView mHeightView; // View for skater's height
    private TextView mCoachView; // View for skater's coach
    private TextView mChoreographerView; // View for skater's choreographer
    private TextView mFormerCoachesView; // View for skater's former coaches
    private TextView mShortProgramView; // View for skater's short program
    private TextView mFreeProgramView; // View for skater's free program
    private TextView mBestShortView; // View for skater's best short program
    private TextView mBestShortCompView; // View for skater's best short comp
    private TextView mBestTopView; // View for skater's best top
    private TextView mBestTopCompView; // View for skater's best top comp
    private TextView mBestLongView;
    private TextView mBestLongCompView;

    public SkaterBioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment SkaterBioFragment.
     */
    public static SkaterBioFragment newInstance() {
        return new SkaterBioFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set up reference to database
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

        // set up following icon
        setUpFollowingIcon(rootView);

        // get all necessary Views
        mSkaterNameView = rootView.findViewById(R.id.skaterName);
        mSkaterNationView = rootView.findViewById(R.id.skaterNation);
        mDobView = rootView.findViewById(R.id.dob);
        mHometownView = rootView.findViewById(R.id.hometown);
        mHeightView = rootView.findViewById(R.id.height);
        mCoachView = rootView.findViewById(R.id.coach);
        mChoreographerView = rootView.findViewById(R.id.choreographer);
        mFormerCoachesView = rootView.findViewById(R.id.formerCoaches);
        mShortProgramView = rootView.findViewById(R.id.shortprogram);
        mFreeProgramView = rootView.findViewById(R.id.freeprogram);
        mBestShortView = rootView.findViewById(R.id.bestShort);
        mBestShortCompView = rootView.findViewById(R.id.bestShortComp);
        mBestTopView = rootView.findViewById(R.id.bestTop);
        mBestTopCompView = rootView.findViewById(R.id.bestTopComp);
        mBestLongView = rootView.findViewById(R.id.bestLong);
        mBestLongCompView = rootView.findViewById(R.id.bestLongComp);

        mSkaterImage = rootView.findViewById(R.id.skaterPhoto);

        // get the skater's name that was passed in, set image
        mSkaterName = getArguments().getString("name");

        if (mSkaterName != null && mSkaterName.contains("&")) {
            mSkaterImage.setImageResource(R.drawable.pair_skaters);
        } else {
            mSkaterImage.setImageResource(R.drawable.single_skater);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // get the skater's ID from the database
        getSkaterFromDB();
    }

    /**
     * Sets up the follow icon for a skater.
     * @param rootView - main View
     */
    private void setUpFollowingIcon (View rootView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MaterialFavoriteButton followButton = rootView.findViewById(R.id.followButton);

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // check database to see if current skater is a user favorite
            mDatabase.child("favorites").child("skaters").child(email[0])
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // set up button
                            if (dataSnapshot.exists()) {
                                // check if skater is a favorite
                                boolean fav = false;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (child.getKey().equals(mSkaterName)) {
                                        fav = true;
                                    }
                                }

                                followButton.setFavorite(fav);
                            }

                            // set up listener for button
                            followButton.setOnFavoriteChangeListener(
                                    (MaterialFavoriteButton buttonView, boolean favorite) -> {
                                        if (favorite) {
                                            addFavorite();
                                        } else {
                                            removeFavorite();
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });


        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Adds current skater to favorites for current user.
     */
    private void addFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // add skater to favorites
            mDatabase.child("favorites").child("skaters").child(email[0]).child(mSkaterName).setValue(true);
        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Removes current skater from favorites for current user.
     */
    private void removeFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // make sure the user is logged in
        if (user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            // remove skater from favorites
            mDatabase.child("favorites")
                    .child("skaters")
                    .child(email[0])
                    .child(mSkaterName)
                    .removeValue();
        } else {
            //Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Fetches the skater ID from the database.
     */
    public void getSkaterFromDB() {
        mDatabase.child("skaters").orderByValue().equalTo(mSkaterName)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) { // should only execute once
                    mSkaterID = childSnapshot.getKey();
                    String mUrl = "http://www.isuresults.com/bios/isufs"+mSkaterID+".htm";
                    (new ParsePageAsyncTask()).execute(new String[]{mUrl});
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * AsyncTask to create the skater bio for this fragment. This is used
     * because getting data from the web is necessary and cannot be done
     * on the main UI.
     */
    private class ParsePageAsyncTask extends AsyncTask<String, Void, Skater> {

        @Override
        protected Skater doInBackground(String... strings) {
            Skater newSkater = new Skater();
            try {
                // set up jsoup
                Document doc = Jsoup.connect(strings[0]).get();

                // Get info from webpage
                if (mSkaterName.contains("&")) {
                    newSkater = createPairsSkater(doc);
                } else {
                    newSkater = createSingleSkater(doc);
                }

               // Log.i(TAG, "Information successfully pulled from ISU website");

            } catch (Throwable t) {
                //Log.e(TAG, t.getMessage());
                t.printStackTrace();
            }

            return newSkater;
        }

        /**
         * Creates a Skater for a pair of skaters (pairs or ice dance).
         * @param doc - jsoup html for the webpage
         * @return - Skater generated
         */
        private Skater createPairsSkater(Document doc) {
            // info per skater
            Element dob1 = doc.getElementById("FormView2_FormView3_person_dobLabel1");
            Element dob2 = doc.getElementById("FormView2_FormView4_person_dobLabel1");
            String dobs = dob1.text() + " & " + dob2.text();
            Element heightNum1 = doc.getElementById("FormView2_FormView3_person_heightLabel1");
            Element heightNum2 = doc.getElementById("FormView2_FormView4_person_heightLabel1");

            String height1, height2;
            if (heightNum1 != null) {
                height1 = heightNum1.text();
                Element heightUnit = doc.getElementById("FormView2_FormView3_Label6");
                height1 += heightUnit.text();
            } else {
                height1 = " ";
            }

            if (heightNum2 != null) {
                height2 = heightNum2.text();
                Element heightUnit = doc.getElementById("FormView2_FormView4_Label6");
                height2 += heightUnit.text();
            } else {
                height2 = " ";
            }
            String heights = height1 + " & " + height2;

            Element hometown1 = doc.getElementById("FormView2_FormView3_person_htometownLabel1");
            Element hometown2 = doc.getElementById("FormView2_FormView4_person_htometownLabel1");
            String hometowns = hometown1.text() + " & " + hometown2.text();

            // info for both skaters
            Element coach = doc.getElementById("FormView2_person_media_information_coachLabel");
            Element choreo = doc.getElementById("FormView2_person_media_information_choreographerLabel");
            Element former = doc.getElementById("FormView2_person_media_information_former_coachLabel");
            Element nation = doc.getElementById("FormView2_FormView3_person_nationLabel1");
            Element shortProgram = doc.getElementById("FormView2_Label3");
            Element freeProgram = doc.getElementById("FormView2_Label4");

            Element scoreTable = doc.getElementById("FormView2_GridView3");
            Elements scoreTableRows = scoreTable.select("tr");
            Element bestTop = scoreTableRows.get(0).select("td").get(1);
            Element bestShort = scoreTableRows.get(1).select("td").get(1);
            Element bestLong = scoreTableRows.get(2).select("td").get(1);

            Element bestShortComp = doc.getElementById("FormView2_GridView3_ctl03_HyperLink3");
            // alternate html id for bestShortComp
            if (bestShortComp == null){
                bestShortComp = doc.getElementById("FormView2_GridView3_ctl03_Label");
            }

            Element bestTopComp = doc.getElementById("FormView2_GridView3_ctl02_HyperLink3");
            // alternate html id for bestTopComp
            if (bestTopComp == null) {
                bestTopComp = doc.getElementById("FormView2_GridView3_ctl02_Label");
            }

            Element bestLongComp = doc.getElementById("FormView2_GridView3_ctl04_HyperLink3");
            // alternate html id for bestLongComp
            if (bestLongComp == null) {
                bestLongComp = doc.getElementById("FormView2_GridView3_ctl04_Label");
            }

            // create Skater based on info
            return new Skater(mSkaterName, dobs, heights, hometowns, coach.text(), choreo.text(),
                    former.text(), nation.text(), shortProgram.text(), freeProgram.text(),
                    bestTop.text(), bestTopComp.text(), bestShort.text(), bestShortComp.text(), bestLong.text(),
                    bestLongComp.text());
        }

        /**
         * Creates a Skater for a single skater (as opposed to pairs/ice dance).
         * @param doc - jsoup html for the webpage
         * @return - Skater generated
         */
        private Skater createSingleSkater(Document doc) {
            Element dob = doc.getElementById("FormView1_person_dobLabel");
            Element heightNum = doc.getElementById("FormView1_person_heightLabel");

            String height;
            if (heightNum != null) {
                height = heightNum.text();
                Element heightUnit = doc.getElementById("FormView1_Label20");
                height += heightUnit.text();
            } else {
                height = " ";
            }

            Element hometown = doc.getElementById("FormView1_person_htometownLabel");
            Element coach = doc.getElementById("FormView1_person_media_information_coachLabel");
            Element choreo = doc.getElementById("FormView1_person_media_information_choreographerLabel");
            Element former = doc.getElementById("FormView1_person_media_information_former_coachLabel");
            Element nation = doc.getElementById("FormView1_person_nationLabel");
            Element shortProgram = doc.getElementById("FormView1_Label3");
            Element freeProgram = doc.getElementById("FormView1_Label4");

            Element scoreTable = doc.getElementById("FormView1_GridView3");
            Elements scoreTableRows = scoreTable.select("tr");
            Element bestTop = scoreTableRows.get(0).select("td").get(1);
            Element bestShort = scoreTableRows.get(1).select("td").get(1);
            Element bestLong = scoreTableRows.get(2).select("td").get(1);


            Element bestShortComp = doc.getElementById("FormView1_GridView3_ctl03_HyperLink1");
            // alternate html id for bestShortComp
            if (bestShortComp == null){
                bestShortComp = doc.getElementById("FormView1_GridView3_ctl03_Label");
            }

            Element bestTopComp = doc.getElementById("FormView1_GridView3_ctl02_HyperLink1");
            // alternate html id for bestTopComp
            if (bestTopComp == null) {
                bestTopComp = doc.getElementById("FormView1_GridView3_ctl02_Label");
            }

            Element bestLongComp = doc.getElementById("FormView1_GridView3_ctl04_HyperLink1");
            // alternate html id for bestLongComp
            if (bestLongComp == null) {
                bestLongComp = doc.getElementById("FormView1_GridView3_ctl04_Label");
            }

            // create Skater based on info
            return new Skater(mSkaterName, dob.text(), height, hometown.text(),
                    coach.text(), choreo.text(), former.text(), nation.text(), shortProgram.text(),
                    freeProgram.text(), bestTop.text(), bestTopComp.text(), bestShort.text(),
                    bestShortComp.text(), bestLong.text(), bestLongComp.text());
        }

        @Override
        protected void onPostExecute(Skater s) {
            // populate the page with the info
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
            mBestTopView.setText(s.getmBestTop());
            mBestTopCompView.setText(s.getmBestTopComp());
            mBestShortView.setText(s.getmBestShort());
            mBestShortCompView.setText(s.getmBestShortComp());
            mBestLongView.setText(s.getmBestLong());
            mBestLongCompView.setText(s.getmBestLongComp());
        }
    }
}
