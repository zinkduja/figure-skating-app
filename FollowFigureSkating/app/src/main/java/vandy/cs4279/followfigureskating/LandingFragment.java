package vandy.cs4279.followfigureskating;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LandingFragment extends Fragment {

    private View.OnClickListener mButtonListener;
    private View.OnClickListener mTextListener;

    public LandingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LandingFragment.
     */
    public static LandingFragment newInstance() {
        return new LandingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(container != null) {
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_landing, container, false);

        createListener();

        //TODO - add listeners
        //Button button = rootView.findViewById(R.id.eventButton);
        //button.setOnClickListener(mButtonListener);
        TextView text = rootView.findViewById(R.id.event4);
        text.setOnClickListener(mTextListener);

        //TODO - add to dynamic adding
        createBlanks(rootView);

        return rootView;
    }

    /**
     * Create OnClickListeners for the Button and the TextViews.
     */
    private void createListener() {
        mTextListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the name of the event to the fragment
                EventSummaryFragment esFrag = EventSummaryFragment.newInstance();
                Bundle data = new Bundle();
                data.putString("event", ((TextView) v).getText().toString());
                esFrag.setArguments(data);

                getFragmentManager().beginTransaction()
                        .add(esFrag, "EVENT_SUMMARY_FRAG")
                        .addToBackStack("")
                        .replace(R.id.frame_layout, esFrag)
                        .commit();
            }
        };
    }

    private void createBlanks(View rootView) {
        // add blanks at end (underneath the bottom nav bar)
        LinearLayout layout = rootView.findViewById(R.id.landingLayout);
        TextView textView;
        for(int i=0; i < 3; i++) {
            textView = new TextView(layout.getContext());
            textView.setText("blank");
            textView.setTextColor(Color.WHITE);
            layout.addView(textView);
        }
    }
}
