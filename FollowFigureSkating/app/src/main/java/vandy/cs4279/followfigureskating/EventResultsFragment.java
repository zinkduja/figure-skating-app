package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventResultsFragment extends Fragment implements View.OnClickListener {

    private FragmentTabHost mTabHost;

    public EventResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventResultsFragment.
     */
    public static EventResultsFragment newInstance() {
        return new EventResultsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //set up the TabHost
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tabhost);

        mTabHost.addTab(mTabHost.newTabSpec("women").setIndicator("Women"),
                WomenResultsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("men").setIndicator("Men"),
                MenResultsFragment.class, null);

        //inflate the layout and set onClickListener
        View womenView = inflater.inflate(R.layout.fragment_women_results, container, false);
        TextView text = (TextView) womenView.findViewById(R.id.wTableCell2);
        text.setOnClickListener(this);

        return mTabHost;
    }

    @Override
    public void onClick(View view) {
        SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(sbFrag, "SKATER_BIO_FRAG")
                // Add this transaction to the back stack
                .addToBackStack("")
                .replace(R.id.frame_layout, sbFrag)
                .commit();
    }
}
