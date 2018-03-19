package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventResultsFragment extends Fragment {

    private String TAG = "EventResultsFragment";

    private View mView;
    private View.OnClickListener mListener;

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
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_event_results, container, false);
        setUpTabHost();

        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the skater to the fragment
                SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
                Bundle data = new Bundle();
                data.putString("name", ((TextView) v).getText().toString());
                sbFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .add(sbFrag, "SKATER_BIO_FRAG")
                        .addToBackStack("")
                        .replace(R.id.resultsPage, sbFrag)
                        .commit();
            }
        };

        //TODO - replace with actual data
        setListeners();

        return mView;
    }

    /**
     * Set up the TabHost for this page.
     */
    private void setUpTabHost() {
        TabHost host = (TabHost)mView.findViewById(R.id.resultsTabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("women");
        spec.setContent(R.id.womenTab);
        spec.setIndicator("Women");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("men");
        spec.setContent(R.id.menTab);
        spec.setIndicator("Men");
        host.addTab(spec);
    }

    private void setListeners() {
        TextView textView = (TextView) mView.findViewById(R.id.wTableCell2);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell8);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell14);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell20);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell26);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell32);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell38);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.wTableCell44);
        textView.setOnClickListener(mListener);

        textView = (TextView) mView.findViewById(R.id.tableCell2);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell8);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell14);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell20);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell26);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell32);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell38);
        textView.setOnClickListener(mListener);
        textView = (TextView) mView.findViewById(R.id.tableCell44);
        textView.setOnClickListener(mListener);
    }
}
