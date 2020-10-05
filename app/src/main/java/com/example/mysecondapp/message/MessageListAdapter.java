package com.example.mysecondapp.message;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mysecondapp.R;

import java.util.ArrayList;

// MessageListAdapter is the adapter that is attached to the chatlist listviews
// Need custom adapter as we use custom objects (not strings) to decide which sort of chat bubble
// (received or sent) to use..
public class MessageListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Message> msgs;

    public MessageListAdapter(Context context, ArrayList<Message> msgs){
        this.context = context;
        this.msgs = msgs;
    }

    public ArrayList<Message> getList(){
        return msgs;
    }
    @Override

    public int getCount() {
        return msgs.size();
    }

    @Override
    public Message getItem(int position) {
        return msgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Message m = msgs.get(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (msgs.get(position).getType().equals("in")) {
            convertView = mInflater.inflate(R.layout.incoming_msg,null);
            TextView textMsg = (TextView) convertView.findViewById(R.id.textMsg);
            textMsg.setText(m.getMessage());
        } else if (msgs.get(position).getType().equals("out")) {
            convertView = mInflater.inflate(R.layout.outgoing_msg,null);
            TextView textMsg = (TextView) convertView.findViewById(R.id.textMsg);
            textMsg.setText(m.getMessage());
        } else {
            convertView = mInflater.inflate(R.layout.yes_no_selection,null);

        }

        return convertView;
    }
}
