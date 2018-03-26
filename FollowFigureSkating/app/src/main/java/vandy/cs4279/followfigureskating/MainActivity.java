package vandy.cs4279.followfigureskating;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF01798C")));

        //initialize auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        //check if user is signed in (non-null) and update UI accordingly
        FirebaseUser curUser = mAuth.getCurrentUser();
        if(curUser != null) {
            Intent intent = new Intent(this, LandingActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Starts the LoginActivity.
     * @param view - the current View
     */
    public void launchLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
