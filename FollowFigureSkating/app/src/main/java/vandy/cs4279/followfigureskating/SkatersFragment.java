package vandy.cs4279.followfigureskating;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SkatersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SkatersFragment extends Fragment implements View.OnClickListener {

    public SkatersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SkatersFragment.
     */
    public static SkatersFragment newInstance() {
        return new SkatersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and set onClickListener
        View rootView = inflater.inflate(R.layout.fragment_skaters, container, false);
        TextView text = (TextView) rootView.findViewById(R.id.skaterName1);
        text.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.skaterPage, new SkaterBioFragment())
                .addToBackStack("")
                .commit();
    }
}
