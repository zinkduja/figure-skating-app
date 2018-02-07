package vandy.cs4279.followfigureskating;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WomenResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WomenResultsFragment extends Fragment implements View.OnClickListener {

    public WomenResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WomenResultsFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        if (container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_women_results, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.wTableCell2);
        text.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        getFragmentManager().beginTransaction().replace(R.id.mainframe, new SkaterBioFragment() ).addToBackStack("").commit();
    }
}
