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

public class LoginFragment extends Fragment {

    private View root;
    private EditText username, password;
    private TextView createAccount;
    private Button login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_login, container, false);
        username = root.findViewById(R.id.username);
        password = root.findViewById(R.id.password);
        login = root.findViewById(R.id.loginButton);
        createAccount = root.findViewById(R.id.createAccount);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (username.getText().length() > 0)
                    username.setError(null);
                if (password.getText().length() > 0)
                    password.setError(null);
            }
        };

        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().length() > 0 && password.getText().length() > 0) {
                    User tempUser;
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText()).matches()) {
                        tempUser = new User(null, username.getText().toString().toLowerCase().trim(), null, null, password.getText().toString(), "logIn");
                    } else {
                        tempUser = new User(null, null, null, username.getText().toString().toLowerCase().trim(), password.getText().toString(), "logIn");
                    }
                    LoginActivity activity = (LoginActivity) getActivity();
                    activity.sendMsg(tempUser);
                    return;
                }
                if (username.getText().length() == 0)
                    username.setError("Enter your email/username");
                if (password.getText().length() == 0)
                    password.setError("Enter your password");
            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, ((LoginActivity)getActivity()).signupFragment); // give your fragment container id in first parameter
                    transaction.commit();
                } catch (Exception ignored) { }
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                return false;
            }
        });
        return root;
    }

    private void hideKeyboard(){
        root.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }

    public void setErrorMessage(){
        username.setError("Username and/or password incorrect");
        password.setError("Username and/or password incorrect");

    }
}
