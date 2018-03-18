package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenResultsFragment extends Fragment {

    public MenResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MenResultsFragment.
     */
    public static MenResultsFragment newInstance() {
        return new MenResultsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout and set onClickListener
        View rootView = inflater.inflate(R.layout.fragment_men_results, container, false);
        //TODO - replace with dynamic rows

        return rootView;
    }

    /*@Override
    public void onClick(View view) {
        //pass the name of the skater to the fragment
        SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
        Bundle data = new Bundle();
        data.putString("name", ((TextView) view).getText().toString());
        sbFrag.setArguments(data);

        //getFragmentManager().popBackStack();
        getFragmentManager().beginTransaction()
                .add(sbFrag, "SKATER_BIO_FRAG")
                // Add this transaction to the back stack
                .addToBackStack("")
                .replace(R.id.resultsMenPage, sbFrag)
                .commit();
    }*/
}
