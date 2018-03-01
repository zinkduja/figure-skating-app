package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventSummaryFragment extends Fragment implements View.OnClickListener  {

    public EventSummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EventSummaryFragment.
     */
    public static EventSummaryFragment newInstance() {
        return new EventSummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event_summary, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.cell2);
        text.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        EventResultsFragment erFrag = EventResultsFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(erFrag, "EVENT_RESULTS_FRAG")
                // Add this transaction to the back stack
                .addToBackStack("")
                .replace(R.id.frame_layout, erFrag)
                .commit();
    }
}
