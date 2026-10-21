package com.safezone.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.safezone.app.R;
import com.safezone.app.ui.common.Ui;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText name, email, password, confirm;
    private TextView error;
    private MaterialButton button;
    private View spinner;
    private AuthFormViewModel vm;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);
        vm = new ViewModelProvider(this).get(AuthFormViewModel.class);

        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.confirm);
        error = findViewById(R.id.error);
        button = findViewById(R.id.registerButton);
        spinner = findViewById(R.id.spinner);

        button.setOnClickListener(v -> vm.register(String.valueOf(name.getText()),
                String.valueOf(email.getText()).trim(), String.valueOf(password.getText()),
                String.valueOf(confirm.getText())));

        vm.loading().observe(this, loading ->
                Ui.setButtonLoading(button, spinner, "Create Account", Boolean.TRUE.equals(loading)));
        vm.error().observe(this, message -> {
            error.setText(message);
            error.setVisibility(message == null ? View.GONE : View.VISIBLE);
        });
        vm.signedIn().observe(this, event -> {
            if (event != null && event.consume() != null) RoleRouter.routeAfterAuth(this);
        });
    }
}
