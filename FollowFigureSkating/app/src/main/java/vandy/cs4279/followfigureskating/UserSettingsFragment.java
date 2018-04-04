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
    private View.OnClickListener mStopSkaterListener;
    private View.OnClickListener mStopEventListener;
    private View.OnClickListener mAboutLister;
    private AlertDialog mFollowingSkatersAlertDialog;
    private AlertDialog mFollowingEventsAlertDialog;
    private AlertDialog mAboutAlertDialog;

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

        // create listeners and Dialogs
        createSkaterDialog();
        createEventDialog();
        createAboutDialog();
        createListeners();

        // set onClickListeners
        view.findViewById(R.id.logout_link).setOnClickListener(mLogoutListener);
        view.findViewById(R.id.stopFollowingSkatersLink).setOnClickListener(mStopSkaterListener);
        view.findViewById(R.id.stopFollowingEventsLink).setOnClickListener(mStopEventListener);
        view.findViewById(R.id.aboutLink).setOnClickListener(mAboutLister);

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

    /**
     * Create an AlertDialog to make sure the user wants to stop
     * following all the skaters they are currently following.
     */
    private void createSkaterDialog() {
        // create Dialog for "STOP" (following skaters)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.skaters)
                .setMessage(R.string.confirm_remove_all)
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
        mFollowingSkatersAlertDialog = builder.create();
    }

    /**
     * Create an AlertDialog to make sure the user wants to stop
     * following all the events they are currently following.
     */
    private void createEventDialog() {
        // create Dialog for "STOP" (following events)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.events)
                .setMessage(R.string.confirm_remove_all)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if(user != null) {
                            // get rid of the ".com" of the email
                            String[] email = user.getEmail().split("\\.");
                            mDatabase.child("favorites")
                                    .child("events")
                                    .child(email[0])
                                    .removeValue();

                            Log.w(TAG, "Stopped following all events.");
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
        mFollowingEventsAlertDialog = builder.create();
    }

    /**
     * Create an AlertDialog to show info about this app.
     */
    private void createAboutDialog() {
        // create Dialog for "About"
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.about_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing, just close Dialog
                    }
                });
        // Create the AlertDialog object and return it
        mAboutAlertDialog = builder.create();
    }

    /**
     * Create the OnClickListeners.
     * mLogoutListener will log the user out
     * mStopSkaterListener and mStopEventListener will show an AlertDialog so the user can confirm
     * mAboutListener will show info about this app
     */
    private void createListeners() {
        mLogoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "User logged out.");
                signOut();
            }
        };

        mStopSkaterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowingSkatersAlertDialog.show();
            }
        };

        mStopEventListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowingEventsAlertDialog.show();
            }
        };

        mAboutLister = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAboutAlertDialog.show();
            }
        };
    }
}
