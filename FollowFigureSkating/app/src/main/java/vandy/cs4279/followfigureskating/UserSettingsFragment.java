package vandy.cs4279.followfigureskating;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettingsFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;

    public UserSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserSettingsFragment.
     */
    public static UserSettingsFragment newInstance() {
        return new UserSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        //set onClickListener
        view.findViewById(R.id.logout_link).setOnClickListener(this);

        return view;
    }

    /**
     * Signs the user out and returns them to the
     * Main Activity page.
     */
    private void signOut() {
        mAuth.signOut();

        //send user back to the main activity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onClick(View view) {
        if(view.getId() == R.id.logout_link) {
            signOut();
        }
    }
}
