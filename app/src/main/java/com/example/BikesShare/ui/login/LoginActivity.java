package com.example.BikesShare.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.BikesShare.MainActivity;
import com.example.BikesShare.R;
import com.example.BikesShare.state.State;
import com.example.BikesShare.state.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.Socket;

// This the start-up Activity that lets the user log-in
// Note that even if the user has already logged in, we still enter the app through here
public class LoginActivity extends AppCompatActivity {

    private Socket clientSocket;
    private BufferedWriter out;
    private DataInputStream in;

    public LoginFragment loginFragment;
    public SignupFragment signupFragment;

    SharedPreferences pref;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();
        // retrieve shared preferences
        pref = getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
        int log_state = pref.getInt(State.LOG_KEY, -1);
        // if not logged in
        if (log_state != State.LOGGED_IN){
            // create the login and signup fragments
            loginFragment = new LoginFragment();
            signupFragment = new SignupFragment();
            // and inflate the login fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, loginFragment); // give your fragment container id in first parameter
            transaction.commit();
        } else {  // if logged in
            String user_string = pref.getString(State.USER_KEY, "null"); // get info about the user saved in the preferences
            User user = new Gson().fromJson(user_string, User.class);
            user.saveUser();
            sendMsg(user);  // send user + pw to the server
        }
    }
    // sends a message to the server
    public void sendMsg(User user){
        new LoginActivity.SendMessage(this).execute(new Gson().toJson(user));
    }

    // Called to perform work in a worker thread.
    private static class SendMessage extends AsyncTask<String, Void, Void> {
        private WeakReference<LoginActivity> activityReference;
        // only retain a weak reference to the activity
        SendMessage(LoginActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoginActivity.GetMsg(activityReference.get()).execute();  // after sending the message, listen for response from server
        }

        protected Void doInBackground(String... strings) {
            LoginActivity activity = activityReference.get();
            try {
                if (activity.clientSocket != null && !activity.clientSocket.isClosed()) {
                    activity.clientSocket.close();
                }
                activity.clientSocket = new Socket("192.168.20.11", 8080);  // computer IP address
                activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                activity.out.write(strings[0]);
                activity.out.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }
    // get message from the server
    private static class GetMsg extends AsyncTask<Void, Void, String> {
        private WeakReference<LoginActivity> activityReference;
        // only retain a weak reference to the activity
        GetMsg(LoginActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(Void... voids) {
            LoginActivity activity = activityReference.get();
            return  activity.readUTF8();
        }

        @Override
        protected void onPostExecute(String s) {
            LoginActivity activity = activityReference.get();
            switch (s) {
                case "received":  // if the username/email are valid for sign up
                    Intent intent = new Intent(activity.getBaseContext(), MainActivity.class);
                    activity.signupFragment.tempUser.confirmUser();  // start the MainActivity!
                    intent.putExtra(State.USER_KEY, new Gson().toJson(activity.signupFragment.tempUser));
                    activity.startActivity(intent);
                    try {
                        activity.clientSocket.close();
                        activity.in.close();
                        activity.out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "both":  // If both username and emails are taken by someone else
                    activity.signupFragment.setErrorMessages(0);  //set error msgs for email and username edittexts
                    break;
                case "username":  // if username is taken by someone else
                    activity.signupFragment.setErrorMessages(-1);
                    break;
                case "email":  // if email already associated with another user
                    activity.signupFragment.setErrorMessages(-2);
                    break;
                case "fail":  // if username and/or pw don't match records on server side
                    activity.loginFragment.setErrorMessage();
                    break;
                default: // the username/email and password matches records in server when logging in
                    try {
                        activity.clientSocket.close();
                        activity.in.close();
                        activity.out.close();
                        JSONObject jsonObj  = new JSONObject(s);  // get the relevant info that has been returned about the user from the server
                        activity.user = new User(jsonObj.getString("name"), jsonObj.getString("email"), "", jsonObj.getString("username"), jsonObj.getString("password"), "loggedIn");
                        if (!jsonObj.getString("dob").equals("None")) // and update the user class attributes using this info
                            activity.user.setDob(jsonObj.getString("dob"));
                        if (!jsonObj.getString("mobile").equals("None"))
                            activity.user.setMobile(jsonObj.getString("mobile"));
                        if (!jsonObj.getString("favStations").equals("None")) {
                            if (jsonObj.getJSONArray("favStations").length() > 0) {
                                JSONArray jsonArray = jsonObj.getJSONArray("favStations");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String favStation = jsonArray.getString(i);
                                    activity.user.addFavStation(favStation);
                                }
                            }
                        }
                        activity.user.setGender(jsonObj.getInt("gender"));
                        intent = new Intent(activity.getBaseContext(), MainActivity.class);
                        intent.putExtra(State.USER_KEY, new Gson().toJson(activity.user));
                        activity.startActivity(intent);  // start the main activity and pass this user class over to it as an extra
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
    // Returns string until end of line (-1) of the input stream
    private String readUTF8(){
        int n;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringWriter writer = new StringWriter();
        while (true){
            try {
                n = reader.read(buffer);
                if (n == -1){
                    return writer.toString();
                }
                writer.write(buffer, 0, n);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString("user", json);
        prefsEditor.apply();
        super.onDestroy();
    }
}