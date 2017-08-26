package com.example.muhta.whereareyou;

/**
 * Created by muhta on 8/1/2017.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] names;
    private final String[] time;
    private final Bitmap[] imageId;
    public CustomList(Activity context,
                      String[] web,String[] web2, Bitmap[] imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.names = web;
        this.time=web2;
        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.textViewName);
        TextView textTime=(TextView)rowView.findViewById(R.id.textViewTime);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        txtTitle.setText(names[position]);
        textTime.setText(time[position]);
        imageView.setImageBitmap(imageId[position]);
        return rowView;
    }
}