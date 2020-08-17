package com.example.mysecondapp.ui.notifications;

import android.app.SearchManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;
import com.example.mysecondapp.ui.profile.ProfileSettings;

import static android.content.Context.SEARCH_SERVICE;

public class NotificationsFragment extends Fragment {

    private MainActivity main;
    private EditText firstNameDisplay;
    private EditText surnameDisplay;
    private EditText emailDisplay;
    private RadioButton femaleDisplay;
    private RadioButton maleDisplay;

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        main = (MainActivity)getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            main.firstName = bundle.getString("FirstNameKey", "");
            main.surname = bundle.getString("SurnameKey", "");
            main.email = bundle.getString("EmailKey", "");
            main.gender = bundle.getString("genderKey", null);
        }
        setHasOptionsMenu(true);

        firstNameDisplay = (EditText) root.findViewById(R.id.display_first_name);
        firstNameDisplay.setText(main.firstName);
        surnameDisplay = (EditText) root.findViewById(R.id.display_surname);
        surnameDisplay.setText(main.surname);
        emailDisplay = (EditText) root.findViewById(R.id.display_email);
        emailDisplay.setText(main.email);
        femaleDisplay = root.findViewById(R.id.display_female);
        maleDisplay = root.findViewById(R.id.display_male);
        if (main.gender != null){
            if (main.gender == "Female"){
                femaleDisplay.setChecked(true);
            } else {
                maleDisplay.setChecked(true);
            }
        }
        return root;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.saveProfileSettings).setVisible(false);
        menu.findItem(R.id.sort).setVisible(false);
        menu.findItem(R.id.filter).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editProfile:
                Bundle args = new Bundle();
                args.putString("SurnameKey", main.surname);
                args.putString("FirstNameKey", main.firstName);
                args.putString("EmailKey", main.email);
                replaceFragment(new ProfileSettings(), new Bundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceFragment(Fragment fragment, Bundle bundle) {
        try {
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.profileSettingsFragmentContainer, fragment); // give your fragment container id in first parameter
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}