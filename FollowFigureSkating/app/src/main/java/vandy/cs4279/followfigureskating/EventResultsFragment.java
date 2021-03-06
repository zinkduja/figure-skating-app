package vandy.cs4279.followfigureskating;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private String mURL; // the main url for the event results

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

        mURL = getArguments().getString("url");
        mHTML = getArguments().getString("html");
        isShort = getArguments().getBoolean("isShort");
        isTeam = getArguments().getBoolean("isTeam");
        isOverall = getArguments().getBoolean("isOverall");

        // set event title
        TextView title = mView.findViewById(R.id.resultsTitle);
        title.setText(mEvent);

        // create the OnClickListener
        mListener = (View v) -> {
            //pass the name of the skater to the fragment
            SkaterBioFragment sbFrag = SkaterBioFragment.newInstance();
            Bundle data = new Bundle();
            String name = ((TextView) v).getText().toString();

            // flip first and last names
            if(mEvent.contains("Olympics")) {
                if (name.contains("/")) { //two people
                    String pair[] = name.split("/");
                    String temp1[] = pair[0].split(" ");
                    String temp2[] = pair[1].split(" ");
                    name = temp1[1] + " " + temp1[0] + " & " + temp2[2] + " " + temp2[1];
                } else { //one person
                    String temp[] = name.split(" ");
                    name = temp[1] + " " + temp[0];
                }
            }

            name = name.replace(System.getProperty("line.separator"), "");
            data.putString("name", name.replace("/", "&"));
            sbFrag.setArguments(data);

            getFragmentManager().beginTransaction()
                    .addToBackStack("SKATER_BIO_FRAG")
                    .replace(R.id.frame_layout, sbFrag)
                    .commit();
            };

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mHTML.equals("other")) {
            LinearLayout layout = mView.findViewById(R.id.resultsScrollLayout);
            TextView text = new TextView(layout.getContext());
            text.setText(R.string.noResultsText);
            text.setTextAppearance(R.style.mediumBaseFont);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            layout.addView(text);
        } else {
            String url = mURL.replace(".htm", "/") + mHTML + ".HTM";
            (new ParsePageAsyncTask()).execute(url);
        }
    }

    private class ParsePageAsyncTask extends AsyncTask<String, Void, Elements> {

        private boolean goodURL;

        @Override
        protected Elements doInBackground(String... strings) {
            goodURL = true;
            Elements firstLine = new Elements();
            try {
                Document doc = Jsoup.connect(strings[0]).get();
                // Get info from webpage
                Element table = doc.select("table table table").get(0);
                firstLine = table.select("tr");
            } catch (Throwable t) {
                //t.printStackTrace();
                goodURL = false;
            }
            return firstLine;
        }

        @Override
        protected void onPostExecute(Elements s) {
            // if bad URL, then set up message for user
            if (!goodURL) {
                LinearLayout layout = mView.findViewById(R.id.resultsScrollLayout);
                TextView text = new TextView(layout.getContext());
                text.setText(R.string.noResultsText);
                text.setTextAppearance(R.style.mediumBaseFont);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                layout.addView(text);
                return;
            }

            // otherwise, get all views
            TableLayout table = mView.findViewById(R.id.resultsTable);

            TextView score1 = mView.findViewById(R.id.wTableHeader4);
            TextView score2 = mView.findViewById(R.id.wTableHeader5);
            TextView score3 = mView.findViewById(R.id.wTableHeader6);

            // set the scores
            if (isOverall) {
                if (isTeam) {
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
            if (isOverall) {
                if (isTeam) {
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
                                if (i != 1) {
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y - 1) + System.getProperty("line.separator") + x.substring(y - 1, x.length());
                                }
                                rowThing.setText(x);
                                if (i == 1) {
                                    rowThing.setOnClickListener(mListener);
                                }
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
                                if (i != 1) {
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y - 1) + System.getProperty("line.separator") + x.substring(y - 1, x.length());
                                }
                                rowThing.setText(x);
                                if (i == 1) {
                                    rowThing.setOnClickListener(mListener);
                                }
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
                                if (i != 1){
                                    rowThing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                }
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y-1) + System.getProperty("line.separator") + x.substring(y-1, x.length());
                                }
                                rowThing.setText(x);
                                if (i == 1) {
                                    rowThing.setOnClickListener(mListener);
                                }
                                rowThing.setTextColor(0xFF000000);
                                rowToAdd.addView(rowThing);
                            }
                        } else {
                            if (i == 0 || i == 2 || i == 3 || i == 4 || i == 5 || i == 7) {
                                TextView rowThing = new TextView(getActivity());
                                String x = cols.get(i).text();
                                int y = x.indexOf('/');
                                if (y != -1) {
                                    x = x.substring(0, y-1) + System.getProperty("line.separator") + x.substring(y-1, x.length());
                                }
                                rowThing.setText(x);
                                if (i == 2) {
                                    rowThing.setOnClickListener(mListener);
                                }
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
