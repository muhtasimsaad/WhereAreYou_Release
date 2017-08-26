package com.example.muhta.whereareyou;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by muhta on 8/23/2017.
 */

public class nullList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] names;

    public nullList(Activity context,
                      String[] web ) {
        super(context, R.layout.blanknotification, web);
        this.context = context;
        this.names = web;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.blanknotification, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.textViewName);
        txtTitle.setText(names[0]);

        return rowView;
    }
}