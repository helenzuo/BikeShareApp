package com.example.mysecondapp.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;

public class ProfileSettings extends Fragment {

//    private String firstName;
//    private String surname;
//    private String email;
    private EditText editTextFirstName;
    private EditText editTextSurname;
    private EditText editTextEmail;
    private RadioButton female;
    private RadioButton male;
    private boolean gender;
    private int walkingDistance;
    private MainActivity main;
    private Menu menu;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        main = (MainActivity)getActivity();

        setHasOptionsMenu(true);
        main.navView.setVisibility(View.GONE);

        editTextFirstName = (EditText) root.findViewById(R.id.edit_text_first_name);
        editTextFirstName.setText(main.firstName);
        editTextSurname = (EditText) root.findViewById(R.id.edit_text_surname);
        editTextSurname.setText(main.surname);
        editTextEmail = (EditText) root.findViewById(R.id.edit_text_email);
        editTextEmail.setText(main.email);
        female = root.findViewById(R.id.checkbox_female);
        male = root.findViewById(R.id.checkbox_male);
        if (main.gender != null) {
            if (main.gender == "Female") {
                female.setChecked(true);
            } else {
                male.setChecked(true);
            }
        }

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });


        return root;
    }

    @Override
    public void onDetach() {
        System.out.println("hi");

        super.onDetach();
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.editProfile).setVisible(false);
        menu.findItem(R.id.saveProfileSettings).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfileSettings:
                Bundle args = new Bundle();
                args.putString("SurnameKey", editTextSurname.getText().toString());
                args.putString("FirstNameKey", editTextFirstName.getText().toString());
                args.putString("EmailKey", editTextEmail.getText().toString());
                if (female.isChecked()){
                    args.putString("genderKey", "Female");
                } else if (male.isChecked()) {
                    args.putString("genderKey", "Male");
                }
                getParentFragmentManager().popBackStackImmediate();
                main.navView.setVisibility(View.VISIBLE);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceFragment(Fragment fragment, Bundle bundle) {
        try {
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.remove(fragment); // give your fragment container id in first parameter
            transaction.commit();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}



