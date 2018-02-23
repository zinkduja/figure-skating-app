package vandy.cs4279.followfigureskating;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import vandy.cs4279.followfigureskating.dbClasses.Skater;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkatersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkatersFragment extends Fragment implements View.OnClickListener {

    private ArrayList<String> mSkaterList;

    public SkatersFragment() {
        // Required empty public constructor
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and set onClickListener
        View rootView = inflater.inflate(R.layout.fragment_skaters, container, false);
        LinearLayout vertLL = rootView.findViewById(R.id.verticalLL);

        //get skaters and populate page TODO
        getSkatersFromWeb();
        for(String skater : mSkaterList){
            LinearLayout layout = new LinearLayout(vertLL.getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(0, 0, 0, 30);

            //add image formatting
            ImageView pic = new ImageView(layout.getContext());
            pic.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            pic.setForegroundGravity(Gravity.LEFT);
            pic.setBaselineAlignBottom(false);
            pic.setImageResource(R.mipmap.ic_launcher); //TODO
            layout.addView(pic);

            //add text formatting
            TextView name = new TextView(layout.getContext());
            name.setLayoutParams(new LinearLayout.LayoutParams(533, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            name.setGravity(Gravity.CENTER);
            name.setPadding(27, 27, 27, 27);
            name.setText(skater);
            name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            name.setTextAppearance(R.style.baseFont);
            layout.addView(name);

            //set listeners and add to main layout
            name.setOnClickListener(this);
            vertLL.addView(layout);
        }

        //TextView text = (TextView) rootView.findViewById(R.id.skaterName1);
        //text.setOnClickListener(this);
        return rootView;
    }

    public void getSkatersFromWeb(){
        //TODO - get all the skaters from the web and put them in mSkaterList
        mSkaterList = new ArrayList<>();

        mSkaterList.add("Boyang Jin");
        mSkaterList.add("Patrick Chan");
        mSkaterList.add("Yuzuru Hanyu");
        mSkaterList.add("Nathan Chen");
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
                .replace(R.id.skaterPage, sbFrag)
                .addToBackStack("")
                .commit();
    }
}
