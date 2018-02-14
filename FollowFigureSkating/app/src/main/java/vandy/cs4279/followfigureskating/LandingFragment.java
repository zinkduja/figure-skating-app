package vandy.cs4279.followfigureskating;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LandingFragment extends Fragment {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_landing, container, false);
    }

    //gets overriden in Landing Activity
    public void onEventButtonPressed(View view) {
    }

    //gets overriden in Landing Activity
    public void onSkaterButtonPressed(View view) {
    }

    //gets overriden in Landing Activity
    public void onEventResultsButtonPressed(View view) {
    }

    //gets overriden in Landing Activity
    public void onCurSkatingButtonPressed(View view) {
    }
}
