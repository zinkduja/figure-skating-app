package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

    private FragmentTabHost mTabHost;
    private View.OnClickListener mListener;
    private TableLayout mTableLayout;

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
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.tabhost);

        mTabHost.addTab(mTabHost.newTabSpec("women").setIndicator("Women"),
                WomenResultsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("men").setIndicator("Men"),
                MenResultsFragment.class, null);

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

        /*mTabHost.setCurrentTabByTag("men");
        TextView text = (TextView) mTabHost.getCurrentTabView().findViewById(R.id.tableCell2);
        text.setOnClickListener(mListener);*/

        //mTabHost.setCurrentTabByTag("women");
        //createWomenHeading();

        return mTabHost;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

    private void createWomenHeading() {
        /*View rootView = mTabHost.getCurrentTabView().get

        LinearLayout linearLayout = new LinearLayout(rootView.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ScrollView scrollView = new ScrollView(linearLayout.getContext());
        mTableLayout = new TableLayout(scrollView.getContext());
        mTableLayout.setStretchAllColumns(true);

        TableRow tableRow = new TableRow(mTableLayout.getContext());
        TextView textView = new TextView(tableRow.getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextAppearance(R.style.smallBaseFont);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setText("Place");

        tableRow.addView(textView);
        mTableLayout.addView(tableRow);
        scrollView.addView(mTableLayout);
        linearLayout.addView(scrollView);
        rootView.add*/
    }
}
