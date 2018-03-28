package vandy.cs4279.followfigureskating;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettingsFragment extends Fragment {

    private final String TAG = "UserSettingsFragment";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private View.OnClickListener mLogoutListener;
    private View.OnClickListener mStopListener;
    private View.OnClickListener mConfirmListener;
    private AlertDialog mAlertDialog;

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

        // initialize auth and database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        // create listeners and Dialog
        createDialog();
        createListeners();

        // set onClickListeners
        view.findViewById(R.id.logout_link).setOnClickListener(mLogoutListener);
        view.findViewById(R.id.stopFollowingLink).setOnClickListener(mStopListener);

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

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_remove_all_skaters)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user != null) {
                            // get rid of the ".com" of the email
                            String[] email = user.getEmail().split("\\.");
                            mDatabase.child("favorites")
                                    .child("skaters")
                                    .child(email[0])
                                    .removeValue();

                            Log.w(TAG, "Stopped following all skaters.");
                        } else {
                            Log.e(TAG, "User somehow not logged in");
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        mAlertDialog = builder.create();
    }

    /**
     * Create the two OnClickListeners.
     * mLogoutListener will log the user out
     * mStopListener will show an AlertDialog so the user can confirm
     */
    private void createListeners() {
        mLogoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "User logged out.");
                signOut();
            }
        };

        mStopListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.show();
            }
        };
    }
}
