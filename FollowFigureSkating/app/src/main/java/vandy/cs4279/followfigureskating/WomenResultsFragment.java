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
 * Use the {@link WomenResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WomenResultsFragment extends Fragment {

    private FragmentTabHost mHost;

    public WomenResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WomenResultsFragment.
     */
    public static WomenResultsFragment newInstance() {
        return new WomenResultsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout and set onClickListener
        View rootView = inflater.inflate(R.layout.fragment_women_results, container, false);

        return rootView;
    }
}
