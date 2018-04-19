package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * A {@link Fragment} subclass that shows the results for a competition.
 * Use the {@link EventResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventResultsFragment extends Fragment {

    private String TAG = "EventResultsFragment"; // tag for the Logcat

    private View mView; // View for the fragment
    private View.OnClickListener mListener; // listener to go to skater bio

    private static String mEvent; // title of the current event
    private String mHTML; // all the html for the events page

    private boolean isShort; // booleans for category
    private boolean isTeam;
    private boolean isOverall;
    private boolean isPrevColored = false; // used to color every other row

    public EventResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
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

        mHTML = getArguments().getString("html");
        isShort = getArguments().getBoolean("isShort");
        isTeam = getArguments().getBoolean("isTeam");
        isOverall = getArguments().getBoolean("isOverall");

        // set event title
        TextView title = (TextView) mView.findViewById(R.id.resultsTitle);
        title.setText(mEvent);

        // create the OnClickListener
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

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        String mURL = "http://www.isuresults.com/results/season1718/owg2018/"+mHTML+".HTM";
        (new ParsePageAsyncTask()).execute(new String[]{mURL});
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Elements> {

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
            // get all views
            TableLayout table = mView.findViewById(R.id.resultsTable);

            TextView score1 = mView.findViewById(R.id.wTableHeader4);
            TextView score2 = mView.findViewById(R.id.wTableHeader5);
            TextView score3 = mView.findViewById(R.id.wTableHeader6);

            // set the scores
            if(isOverall) {
                if(isTeam) {
                    score1.setText("Total Points");
                    score2.setText("");
                    score3.setText("");
                }
                else {
                    score1.setText("Points");
                    score2.setText("SP");
                    score3.setText("FS");
                }
            }

            // create the table
            Elements cols;
            if(isOverall) {
                if(isTeam) {
                    for (int j = 1; j < s.size(); j += 2) { // used for weird ISU html layout
                        TableRow rowToAdd = new TableRow(getActivity());

                        // color and set up the row
                        if (!isPrevColored) {
                            rowToAdd.setBackgroundColor(getResources().getColor(R.color.paleBlue));
                            isPrevColored = true;
                        }
                        else {
                            isPrevColored = !isPrevColored;
                        }
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                        rowToAdd.setLayoutParams(lp);

                        // create TextViews for the row
                        cols = s.get(j).select("td");
                        for (int i = 0; i <= 6; i++) {
                            if (i == 0 || i == 1 || i == 2 || i == 6) {
                                TextView rowThing = new TextView(getActivity());
                                if(i!=1){
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y - 1) + System.getProperty("line.separator") + x.substring(y - 1, x.length());
                                }
                                rowThing.setText(x);
                                rowThing.setTextColor(0xFF000000);
                                rowToAdd.addView(rowThing);
                            }
                        }
                        table.addView(rowToAdd, j / 2 + 1);
                    }
                }
                else {
                    for (int j = 1; j < s.size(); j += 2) {
                        TableRow rowToAdd = new TableRow(getActivity());

                        // color and set up the row
                        if (!isPrevColored) {
                            rowToAdd.setBackgroundColor(getResources().getColor(R.color.paleBlue));
                            isPrevColored = true;
                        }
                        else {
                            isPrevColored = !isPrevColored;
                        }
                        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                        rowToAdd.setLayoutParams(lp);

                        // create TextViews for the row
                        cols = s.get(j).select("td");
                        for (int i = 0; i <= 8; i++) {
                            if (i == 0 || i == 1 || i == 2 || i == 6 || i == 7 || i == 8) {
                                TextView rowThing = new TextView(getActivity());
                                if(i!=1){
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y - 1) + System.getProperty("line.separator") + x.substring(y - 1, x.length());
                                }
                                rowThing.setText(x);
                                rowThing.setTextColor(0xFF000000);
                                rowToAdd.addView(rowThing);
                            }
                        }
                        table.addView(rowToAdd, j / 2 + 1);
                    }
                }
            }
            else {
                for (int j = 1; j < s.size(); j++) {
                    TableRow rowToAdd = new TableRow(getActivity());

                    // color and set up the row
                    if (!isPrevColored) {
                        rowToAdd.setBackgroundColor(getResources().getColor(R.color.paleBlue));
                        isPrevColored = true;
                    }
                    else {
                        isPrevColored = !isPrevColored;
                    }
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                    rowToAdd.setLayoutParams(lp);
                    cols = s.get(j).select("td");

                    // create TextViews for the row
                    for (int i = 0; i <= 8; i++) {
                        if (!isShort || isTeam) {
                            if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 6) {
                                TextView rowThing = new TextView(getActivity());
                                if(i!=1){
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if(y!=-1) {
                                    x = x.substring(0, y-1) + System.getProperty("line.separator") + x.substring(y-1, x.length());
                                }
                                rowThing.setText(x);
                                rowThing.setTextColor(0xFF000000);
                                rowToAdd.addView(rowThing);
                            }
                        } else {
                            if (i == 0 || i == 2 || i == 3 || i == 4 || i == 5 || i == 7) {
                                TextView rowThing = new TextView(getActivity());
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if(y!=-1) {
                                    x = x.substring(0, y-1) + System.getProperty("line.separator") + x.substring(y-1, x.length());
                                }
                                rowThing.setText(x);
                                rowThing.setTextColor(0xFF000000);
                                rowToAdd.addView(rowThing);
                            }
                        }
                    }
                    table.addView(rowToAdd, j);
                }
            }

            // add blanks (because of the BottomNavigationView)
            for(int i=0; i < 3; i++) {
                TableRow blank = new TableRow(table.getContext());
                TextView empty = new TextView(blank.getContext());
                blank.addView(empty);
                table.addView(blank);
            }
        }

        }
}
