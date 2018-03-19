package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventSummaryFragment extends Fragment {

    private final String TAG = "EventSummaryFragment";
    private final String RESULTS = "Results";
    private final String CUR_SKATE = "Currently Skating";

    private View.OnClickListener mListener;
    private String mEvent;

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
        if (container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event_summary, container, false);
        mEvent = getArguments().getString("event");
        createListener();

        // set event title
        TextView title = (TextView) rootView.findViewById(R.id.eventTitle);
        title.setText(mEvent);

        // set up the table
        try{
            createTable(rootView);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return rootView;
    }

    private void createListener() {
        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                data.putString("event", mEvent);
                String type = ((TextView) v).getText().toString();
                if (type.equals(RESULTS)) {
                    EventResultsFragment erFrag = EventResultsFragment.newInstance();
                    getFragmentManager().beginTransaction()
                            .add(erFrag, "EVENT_RESULTS_FRAG")
                            .addToBackStack("")
                            .replace(R.id.summaryPage, erFrag)
                            .commit();
                } else if (type.equals(CUR_SKATE)) {
                    CurrentlySkatingFragment csFrag = CurrentlySkatingFragment.newInstance();
                    getFragmentManager().beginTransaction()
                            .add(csFrag, "CURRENTLY_SKATING_FRAG")
                            .addToBackStack("")
                            .replace(R.id.summaryPage, csFrag)
                            .commit();
                } else {
                    Log.e(TAG, "TextView is not \'Results\' or \'Currently Skating\'.");
                }
            }
        };
    }

    private void createTable(View rootView) throws IOException {
        /*//TODO - change url based on event
        Document doc = Jsoup.connect("http://www.isuresults.com/results/season1718/owg2018/").get();
        TableLayout table = rootView.findViewById(R.id.eventTable);

        // should only be one table
        Elements webTables = doc.select("table[width=100%]");
        webTables.get(0).child(0).children().forEach(row -> {
            // date, time, category, segment

        });*/
    }

    private void createTableRow(TableLayout table) {
        TableRow row = new TableRow(table.getContext());
    }
}
