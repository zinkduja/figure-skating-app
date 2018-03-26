package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventResultsFragment extends Fragment {

    private String TAG = "EventResultsFragment";

    private View mView;
    private View.OnClickListener mListener;
    private static String mEvent;

    private TextView mRank1;
    private TextView mName1;
    private TextView mNation1;
    private TextView mPoints1;
    private TextView mSP1;
    private TextView mFS1;

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
        if(getArguments() != null) {
            mEvent = getArguments().getString("event");
        }

        // set event title
        TextView title = (TextView) mView.findViewById(R.id.resultsTitle);
        title.setText(mEvent);

        mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the skater to the fragment
                SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
                Bundle data = new Bundle();
                data.putString("name", ((TextView) v).getText().toString());
                sbFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .addToBackStack("SKATER_BIO_FRAG")
                        .replace(R.id.frame_layout, sbFrag)
                        .commit();
            }
        };

        //TODO - replace with actual data

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        (new ParsePageAsyncTask()).execute(new String[]{"http://www.isuresults.com/results/season1718/owg2018/CAT001RS.HTM"});
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Elements> {
        Element image;

        @Override
        protected Elements doInBackground(String... strings) {
            Elements firstLine = new Elements();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                // Get info from webpage
                Element table = doc.select("table table table").get(0);
                firstLine = table.select("tr");
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return firstLine;
        }

        @Override
        protected void onPostExecute(Elements s) {
            TableLayout table = mView.findViewById(R.id.resultsTable);
            Elements cols;
            for (int j = 1 ; j < s.size(); j+=2) {
                TableRow rowToAdd = new TableRow(getActivity());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                rowToAdd.setLayoutParams(lp);
                cols = s.get(j).select("td");
                for (int i = 0; i <= 8; i++) {
                    if (i == 0 || i == 1 || i == 2 || i == 6 || i == 7 || i == 8) {
                        TextView rowThing = new TextView(getActivity());
                        rowThing.setText(cols.get(i).text());
                        rowThing.setTextColor(0xFF000000);
                        rowToAdd.addView(rowThing);
                    }
                }
                table.addView(rowToAdd, j/2+1);
            }
        }

        }
}
