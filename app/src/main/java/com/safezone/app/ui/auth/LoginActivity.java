package com.safezone.app.ui.auth;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.safezone.app.R;
import com.safezone.app.ui.common.Ui;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText email, password;
    private TextView error;
    private MaterialButton loginButton, guestButton;
    private TextView registerLink;
    private View spinner;
    private AuthFormViewModel vm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        vm = new ViewModelProvider(this).get(AuthFormViewModel.class);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        error = findViewById(R.id.error);
        loginButton = findViewById(R.id.loginButton);
        guestButton = findViewById(R.id.guestButton);
        registerLink = findViewById(R.id.registerLink);
        spinner = findViewById(R.id.spinner);

        int navy = ContextCompat.getColor(this, R.color.navy);

        String prefix = "Don't have an account? ";
        SpannableString link = new SpannableString(prefix + "Register");
        link.setSpan(new ForegroundColorSpan(navy), prefix.length(), link.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        link.setSpan(new StyleSpan(Typeface.BOLD), prefix.length(), link.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerLink.setText(link);

        loginButton.setOnClickListener(v -> vm.login(text(email), String.valueOf(password.getText())));
        guestButton.setOnClickListener(v -> vm.continueAsGuest());
        registerLink.setOnClickListener(v -> {
            if (!vm.isLoading()) startActivity(new Intent(this, RegisterActivity.class));
        });

        vm.loading().observe(this, loading -> {
            boolean l = Boolean.TRUE.equals(loading);
            Ui.setButtonLoading(loginButton, spinner, "Log In", l);
            guestButton.setEnabled(!l);
            registerLink.setEnabled(!l);
        });
        vm.error().observe(this, message -> {
            error.setText(message);
            error.setVisibility(message == null ? View.GONE : View.VISIBLE);
        });
        vm.signedIn().observe(this, event -> {
            if (event != null && event.consume() != null) RoleRouter.routeAfterAuth(this);
        });
    }

    private static String text(TextInputEditText f) {
        return f.getText() == null ? "" : f.getText().toString().trim();
    }
}
