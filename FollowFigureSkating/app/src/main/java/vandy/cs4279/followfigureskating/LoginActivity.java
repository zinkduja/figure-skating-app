package vandy.cs4279.followfigureskating;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private AutoCompleteTextView mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Views
        mEmailField = findViewById(R.id.email);
        mPasswordField = findViewById(R.id.password);

        //Buttons
        findViewById(R.id.signIn_button).setOnClickListener(this);
        findViewById(R.id.register_button).setOnClickListener(this);

        //initialize auth
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Attempts to create a new user account.
     * @param email - the email the user registers with
     * @param password - the password the user registers with
     */
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if(!validateForm() || !checkPassword()) {
            return;
        }

        //showProgressDialog();

        //create a user with given email
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user)
                            onSuccessfulLoginOrRegister();
                        } else {
                            Log.w(TAG, "CreateUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null)
                        }

                        //hideProgressDialog();
                    }
                });
    }

    /**
     * Makes sure the password meets all requirements.
     * @return - true if password meets requirements, false otherwise
     */
    private boolean checkPassword() {
        //TODO - add more password requirements?
        if(mPasswordField.length() < 6) {
            Toast.makeText(LoginActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
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
        if(!validateForm()) {
            return;
        }

        //showProgressDialog();

        //sign in with email
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user)
                            onSuccessfulLoginOrRegister();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null)
                        }

                        //hideProgressDialog();
                    }
                });
    }

    //TODO - do we want this?

    /**
     * Sends an email verifaction email to the user to ensure
     * that the user gave a valid email that they have
     * access to.
     */
    private void sendEmailVerification() {
        /*
        // Disable button
        findViewById(R.id.verify_email_button).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.verify_email_button).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
         */
    }

    /**
     * Makes sure that the email and password field have both
     * been filled out in the form.
     * @return - true if both fields are filled out, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if(TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if(TextUtils.isEmpty(password)) {
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
    }

    public void onClick(View view) {
        int i = view.getId();

        if(i == R.id.signIn_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if(i == R.id.register_button) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}
