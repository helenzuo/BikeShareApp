package com.example.mysecondapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;
import com.example.mysecondapp.State;
import com.example.mysecondapp.Station;
import com.example.mysecondapp.User;
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

        pref = getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
        int log_state = pref.getInt(State.LOG_KEY, -1);

        if (log_state != State.LOGGED_IN){
            loginFragment = new LoginFragment();
            signupFragment = new SignupFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, loginFragment); // give your fragment container id in first parameter
            transaction.commit();
        } else {
            String user_string = pref.getString(State.USER_KEY, "null");
            User user = new Gson().fromJson(user_string, User.class);
            user.saveUser();
            sendMsg(user);
        }
    }

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
            new LoginActivity.GetMsg(activityReference.get()).execute();
        }

        protected Void doInBackground(String... strings) {
            LoginActivity activity = activityReference.get();
            try {
                if (activity.clientSocket != null && !activity.clientSocket.isClosed()) {
                    activity.clientSocket.close();
                }
                activity.clientSocket = new Socket("192.168.20.11", 8080);
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
                case "received":
                    Intent intent = new Intent(activity.getBaseContext(), MainActivity.class);
                    activity.signupFragment.tempUser.confirmUser();
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
                case "both":
                    activity.signupFragment.setErrorMessages(0);
                    break;
                case "username":
                    activity.signupFragment.setErrorMessages(-1);
                    break;
                case "email":
                    activity.signupFragment.setErrorMessages(-2);
                    break;
                case "fail":
                    activity.loginFragment.setErrorMessage();
                    break;
                default: // success case
                    try {
                        activity.clientSocket.close();
                        activity.in.close();
                        activity.out.close();
                        System.out.println(s);

                        JSONObject jsonObj  = new JSONObject(s);
                        activity.user = new User(jsonObj.getString("name"), jsonObj.getString("email"), "", jsonObj.getString("username"), jsonObj.getString("password"), "loggedIn");
                        if (!jsonObj.getString("dob").equals("None"))
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
                        activity.startActivity(intent);

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

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