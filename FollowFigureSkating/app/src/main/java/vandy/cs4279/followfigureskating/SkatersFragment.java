package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkatersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkatersFragment extends Fragment implements View.OnClickListener {

    private String[] mSkaterList;

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

        //get skaters and populate page TODO
        /*getSkatersFromWeb();
        for(int i = 0; i < mSkaterList.length) {
            LinearLayout layout = new LinearLayout();
            layout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView pic = new ImageView(layout.getContext());
            pic.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            pic.setForegroundGravity(Gravity.LEFT);
            pic.setBaselineAlignBottom(false);
            pic.setImageResource(R.mipmap.ic_launcher);  //TODO
            layout.addView(pic);

            TextView name = new TextView(layout.getContext());
            name.setLayoutParams(new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.MATCH_PARENT));
            name.setForegroundGravity(Gravity.CENTER_VERTICAL);
            name.setPadding(20, 20, 20, 20);

        }*/

        TextView text = (TextView) rootView.findViewById(R.id.skaterName1);
        text.setOnClickListener(this);
        return rootView;
    }

    public void getSkatersFromWeb(){
        //TODO - get all the skaters from the web and put them in mSkaterList
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
