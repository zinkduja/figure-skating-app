package vandy.cs4279.followfigureskating;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A {@link Fragment} subclass that displays the settings for a user.
 * Use the {@link UserSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettingsFragment extends Fragment {

    private final String TAG = "UserSettingsFragment"; // tag for the Logcat

    private View.OnClickListener mLogoutListener; // listener to logout
    private View.OnClickListener mStopSkaterListener; // listener to stop following all skaters
    private View.OnClickListener mStopEventListener; // listener to stop following all events
    private View.OnClickListener mAboutLister; // listener to pull up app info
    private View.OnClickListener mChangePasswordListener; // listener to change user password

    private AlertDialog mFollowingSkatersAlertDialog; // AlertDialog for stop following all skaters
    private AlertDialog mFollowingEventsAlertDialog; // AlertDialog for stop following all skaters
    private AlertDialog mAboutAlertDialog; // AlertDialog for pulling up app info
    private AlertDialog mChangePasswordDialog; // AlertDialog for changing a user's password

    private DatabaseReference mDatabase; // reference to Firebase database
    private FirebaseAuth mAuth; // Firebase authentication reference

    public UserSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
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
        createChangePasswordDialog();
        createListeners();

        // set onClickListeners
        view.findViewById(R.id.logout_link).setOnClickListener(mLogoutListener);
        view.findViewById(R.id.stopFollowingSkatersLink).setOnClickListener(mStopSkaterListener);
        view.findViewById(R.id.stopFollowingEventsLink).setOnClickListener(mStopEventListener);
        view.findViewById(R.id.aboutLink).setOnClickListener(mAboutLister);
        view.findViewById(R.id.changePasswordLink).setOnClickListener(mChangePasswordListener);

        return view;
    }

    /**
     * Signs the user out and returns them to the Main Activity page.
     */
    private void signOut() {
        mAuth.signOut();

        //send user back to the main activity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Creates an AlertDialog that allows the user to change
     * their password.  The user must re-authenticate.
     */
    private void createChangePasswordDialog() {
        // create layouts
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextInputLayout inLayout1 = new TextInputLayout(linearLayout.getContext());
        TextInputLayout inLayout2 = new TextInputLayout(linearLayout.getContext());

        // create TextInputEditTexts
        TextInputEditText oldPassword = new TextInputEditText(inLayout1.getContext());
        oldPassword.setHint("Old password");
        oldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        TextInputEditText newPassword = new TextInputEditText(inLayout2.getContext());
        newPassword.setHint("New password");
        newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // add to main linearLayout for Dialog
        inLayout1.addView(oldPassword);
        inLayout2.addView(newPassword);
        linearLayout.addView(inLayout1);
        linearLayout.addView(inLayout2);

        // create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Your Password")
                .setView(linearLayout)
                .setPositiveButton("Update", (DialogInterface dialog, int id) -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // make sure the user is logged in
                    if (user != null) {
                        // make sure old password is correct
                        AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(),
                                oldPassword.getText().toString());

                        user.reauthenticate(cred)
                                .addOnSuccessListener(aVoid -> {
                                    // user re-authenticated, change password
                                    user.updatePassword(newPassword.getText().toString());
                                    Toast.makeText(this.getActivity(), "Password changed.",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(aVoid -> {
                                    // wrong password
                                    Toast.makeText(this.getActivity(), "Incorrect password.",
                                            Toast.LENGTH_LONG).show();
                                });

                        Log.d(TAG, user.getEmail() + " changed password.");
                    } else {
                        Log.e(TAG, "User somehow not logged in");
                    }
                })
                .setNegativeButton("Cancel", (DialogInterface dialog, int id) -> {
                    // user cancelled, do nothing
                });

        // Set the AlertDialog object to instance variable
        mChangePasswordDialog = builder.create();
    }

    /**
     * Creates an AlertDialog to make sure the user wants to stop
     * following all the skaters they are currently following.
     */
    private void createSkaterDialog() {
        // create Dialog for "STOP" (following skaters)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.skaters)
                .setMessage(R.string.confirm_remove_all)
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int id) -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // make sure the user is logged in
                    if (user != null) {
                        // get rid of the ".com" of the email
                        String[] email = user.getEmail().split("\\.");
                        mDatabase.child("favorites")
                                .child("skaters")
                                .child(email[0])
                                .removeValue();

                        Log.d(TAG, "Stopped following all skaters.");
                    } else {
                        Log.e(TAG, "User somehow not logged in");
                    }
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int id) -> {
                    // User cancelled the dialog
                });

        // Set the AlertDialog object to instance variable
        mFollowingSkatersAlertDialog = builder.create();
    }

    /**
     * Creates an AlertDialog to make sure the user wants to stop
     * following all the events they are currently following.
     */
    private void createEventDialog() {
        // create Dialog for "STOP" (following events)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.events)
                .setMessage(R.string.confirm_remove_all)
                .setPositiveButton(R.string.yes, (DialogInterface dialog, int id) -> {

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    // make sure the user is logged in
                    if (user != null) {
                        // get rid of the ".com" of the email
                        String[] email = user.getEmail().split("\\.");
                        mDatabase.child("favorites")
                                .child("events")
                                .child(email[0])
                                .removeValue();

                        Log.d(TAG, "Stopped following all events.");
                    } else {
                        Log.e(TAG, "User somehow not logged in");
                    }
                })
                .setNegativeButton(R.string.no, (DialogInterface dialog, int id) -> {
                    // User cancelled the dialog
                });

        // Set the AlertDialog object to instance variable
        mFollowingEventsAlertDialog = builder.create();
    }

    /**
     * Creates an AlertDialog to show info about this app.
     */
    private void createAboutDialog() {
        // create Dialog for "About"
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.about_msg)
                .setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
                    //do nothing, just close Dialog
                });

        // Set the AlertDialog object to instance variable
        mAboutAlertDialog = builder.create();
    }

    /**
     * Creates the OnClickListeners.
     * mLogoutListener will log the user out
     * mStopSkaterListener and mStopEventListener will show an AlertDialog so the user can confirm
     * mAboutListener will show info about this app
     * mChangePasswordListener will allow the user to change their password
     */
    private void createListeners() {
        mLogoutListener = (View v) -> {
            Log.d(TAG, "User logged out.");
            signOut();
        };

        mStopSkaterListener = (View v) -> {
            mFollowingSkatersAlertDialog.show();
        };

        mStopEventListener = (View v) -> {
            mFollowingEventsAlertDialog.show();
        };

        mAboutLister = (View v) -> {
            mAboutAlertDialog.show();
        };

        mChangePasswordListener = (View v) -> {
            mChangePasswordDialog.show();
        };
    }
}
