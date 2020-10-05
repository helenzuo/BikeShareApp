package com.example.mysecondapp.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysecondapp.R;
import com.example.mysecondapp.state.User;

public class SignupFragment extends Fragment implements View.OnClickListener {
    private EditText name, username, email, password1, password2;
    private Button createAccount;
    private TextView login;
    private View root;
    public User tempUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_signup, container, false);

        login = root.findViewById(R.id.login);
        name = root.findViewById(R.id.nameSignup);
        username = root.findViewById(R.id.username);
        email = root.findViewById(R.id.emailSignup);
        password1 = root.findViewById(R.id.passwordSignup);
        password2 = root.findViewById(R.id.password2Signup);
        createAccount = root.findViewById(R.id.signupButton);

        login.setOnClickListener(this);
        createAccount.setOnClickListener(this);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                name.setError(null);
            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                username.setError(null);
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                email.setError(null);
            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                password1.setError(null);
                password2.setError(null);
            }
        };
        password1.addTextChangedListener(textWatcher);
        password2.addTextChangedListener(textWatcher);

        password2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                return false;
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        return root;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public void setErrorMessages(int errors){
        if (errors == 0){
            username.setError("Username already exists");
            email.setError("Member with this email already exists");
        } else if (errors == -1){
            username.setError("Username already exists");
        } else {
            email.setError("Member with this email already exists");
        }
    }

    int isPasswordValid(String password1, String password2) {
        if (password1.length() < 5){
            return -1;
        } else if (!password1.equals(password2)){
            return -2;
        }
        return 1;
    }
    @Override
    public void onClick(View v) {
        if (v == login){
            try {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.container, new LoginFragment()); // give your fragment container id in first parameter
                transaction.commit();
            } catch (Exception ignored) { }
        } else {
            boolean ok = true;
            if (name.getText().toString().trim().length() == 0) {
                name.setError("Enter full name");
                ok = false;
            }
            if (username.getText().toString().trim().length() == 0){
                username.setError("Enter username");
                ok = false;
            }
            if (!isEmailValid(email.getText())) {
                email.setError("Enter valid email address");
                ok = false;
            }
            int passwordValid = isPasswordValid(password1.getText().toString(), password2.getText().toString());
            if (passwordValid == -1){
                password1.setError("Password must be at least 5 characters long");
                ok = false;
            } else if (passwordValid == -2){
                password2.setError("Passwords do not match");
                ok = false;
            }
            if (ok){
                tempUser = new User(name.getText().toString(), email.getText().toString().toLowerCase().trim(), "", username.getText().toString().trim().toLowerCase(), password1.getText().toString(), "signUp");
                LoginActivity activity = (LoginActivity) getActivity();
                activity.sendMsg(tempUser);
            }
        }
    }

    private void hideKeyboard(){
        root.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }
}
