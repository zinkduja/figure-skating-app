package vandy.cs4279.followfigureskating;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * This Activity allows the user to login to the app.  Only users who have registered
 * and logged in can proceed into the app.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword"; // tag for the Logcat

    private AutoCompleteTextView mEmailField; // email text
    private EditText mPasswordField; // password text

    private AlertDialog mGetEmailDialog; // AlertDialog for getting user's email for resetting password

    private FirebaseAuth mAuth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set up the view for the Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF01798C")));

        // create AlertDialogs
        createDialogs();

        // instantiate Views
        mEmailField = findViewById(R.id.email);
        mPasswordField = findViewById(R.id.password);

        // instantiate Buttons
        findViewById(R.id.signIn_button).setOnClickListener(this);
        findViewById(R.id.register_button).setOnClickListener(this);
        findViewById(R.id.forgotPasswordLink).setOnClickListener(this);

        // initialize auth
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Creates an AlertDialog that will allow the user to reset their
     * password via an email sent to the user-inputted email.
     */
    private void createDialogs() {
        // create layout and TextInputEditText
        LinearLayout linearLayout1 = new LinearLayout(this);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        TextInputEditText email = new TextInputEditText(linearLayout1.getContext());
        email.setHint("Email");
        linearLayout1.addView(email);

        // AlertDialog for getting the user's email
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Reset password")
                .setMessage("An email will be sent shortly with a link to reset your password.")
                .setView(linearLayout1)
                .setPositiveButton("Send", (DialogInterface dialog, int id) -> {

                    FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                            .addOnSuccessListener(aVoid -> {
                                // email sent successfully
                                Log.d(TAG, "Email sent to: " + email.getText().toString());
                                Toast.makeText(LoginActivity.this, "Email sent.",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(aVoid -> {
                                // email could not be sent
                                Log.d(TAG, "Email could not be sent to: " + email.getText().toString());
                                Toast.makeText(LoginActivity.this, "No account with that email.",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (DialogInterface dialog, int id) -> {
                    // User cancelled the dialog
                });

        // Set the AlertDialog object to instance variable
        mGetEmailDialog = builder1.create();
    }

    /**
     * Attempts to create a new user account.
     * @param email - the email the user registers with
     * @param password - the password the user registers with
     */
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm() || !checkPassword()) {
            return;
        }

        //create a user with given email
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {

                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        onSuccessfulLoginOrRegister();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Makes sure the password meets all requirements.
     * @return - true if password meets requirements, false otherwise
     */
    private boolean checkPassword() {
        if (mPasswordField.length() < 6) {
            Toast.makeText(LoginActivity.this, "Password must be at least 6 characters.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Attempts to sign a user in with the given email and password.
     * @param email - email the user has entered
     * @param password - password the user has entered
     */
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        //sign in with email
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (@NonNull Task<AuthResult> task) -> {

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        onSuccessfulLoginOrRegister();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Makes sure that the email and password field have both
     * been filled out in the form.
     * @return - true if both fields are filled out, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;

        // email is required
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        // password is required
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    /**
     * If the login or registration was successful, start
     * the LandingActivity to allow the user to access
     * the rest of the app.
     */
    private void onSuccessfulLoginOrRegister() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        finish(); // prevents user from going 'back' to here
    }

    /**
     * Handles when the user clicks on the sign in button or the register button.
     * @param view - the button the user clicks on
     */
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.signIn_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.register_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.forgotPasswordLink) {
            mGetEmailDialog.show();
        }
    }
}
