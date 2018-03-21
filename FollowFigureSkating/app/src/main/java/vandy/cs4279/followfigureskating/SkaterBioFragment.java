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

import com.google.firebase.database.ChildEventListener;
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
    private String mSkaterID;

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

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();

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

        // set up following icon
        setUpFollowingIcon(rootView);

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

        //get the skater's name that was passed in
        mSkaterName = getArguments().getString("name");




        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getSkaterFromDB();
    }

    /**
     * Set up the follow icon for a skater.
     * @param rootView - main View
     */
    private void setUpFollowingIcon (View rootView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MaterialFavoriteButton followButton =
                (MaterialFavoriteButton) rootView.findViewById(R.id.followButton);

        // check if the skater is favorited or not
        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");

            mDatabase.child("favorites").child("skaters").child(email[0])
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // set up button
                            if(dataSnapshot.exists()) {
                                // check if skater is a favorite
                                boolean fav = false;
                                for(DataSnapshot child : dataSnapshot.getChildren()) {
                                    if(child.getValue().equals(mSkaterName)) {
                                        fav = true;
                                    }
                                }

                                followButton.setFavorite(fav);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });

            // set up listener for button
            followButton.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if (favorite) {
                                addFavorite();
                            } else {
                                removeFavorite();
                            }
                        }
                    });
        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Add current skater to favorites for current user.
     */
    private void addFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");
            mDatabase.child("favorites").child("skaters").child(email[0]).push().setValue(mSkaterName);
        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }

    /**
     * Remove current skater from favorites for current user.
     */
    private void removeFavorite() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // get rid of the ".com" of the email
            String[] email = user.getEmail().split("\\.");
            mDatabase.child("favorites").child("skaters").child(email[0]).equalTo(mSkaterName).getRef().removeValue();
        } else {
            Log.e(TAG, "User somehow not logged in");
        }
    }


    public void getSkaterFromDB() {
        mDatabase.child("skaters").orderByValue().equalTo(mSkaterName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    mSkaterID = childSnapshot.getKey();
                    String mUrl = "http://www.isuresults.com/bios/isufs"+mSkaterID+".htm";
                    (new ParsePageAsyncTask()).execute(new String[]{mUrl});
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Skater> {
        Element image;

        @Override
        protected Skater doInBackground(String... strings) {
            Skater newSkater = new Skater();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                // Get info from webpage
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
                if(bestShortComp == (null)){
                    bestShortComp = doc.getElementById("FormView1_GridView3_ctl03_Label");
                }
                Element bestShort = doc.getElementById("FormView1_GridView3");
                Element bestTopComp = doc.getElementById("FormView1_GridView3_ctl02_HyperLink1");
                if(bestTopComp == (null)) {
                    bestTopComp = doc.getElementById("FormView1_GridView3_ctl02_Label");
                }
                Element bestTop = doc.getElementById("FormView1_GridView3");
                System.out.println(freeProgram.text());
                System.out.println(bestTop.text());
                System.out.println(bestTopComp.text());
                System.out.println(bestShort.text());

                //TODO - check if elements are null
                newSkater = new Skater(mSkaterName, dob.text(), height, hometown.text(),
                        coach.text(), choreo.text(), former.text(), nation.text(), shortProgram.text(),
                        freeProgram.text(), bestTop.text(), bestTopComp.text(), bestShort.text(),
                        bestShortComp.text());

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
