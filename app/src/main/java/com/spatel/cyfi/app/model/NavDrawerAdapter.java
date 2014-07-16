package com.spatel.cyfi.app.model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spatel.cyfi.app.R;

import java.util.ArrayList;

/**
 * Created by spatel on 7/11/14.
 */
public class NavDrawerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_items, null);
        }
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView counter = (TextView) convertView.findViewById(R.id.counter);


        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        title.setText(navDrawerItems.get(position).getTitle());

        if(navDrawerItems.get(position).getCounterVisibility()) {
            counter.setText(navDrawerItems.get(position).getCount());
        }
        else {
            counter.setVisibility(View.GONE);
        }

        return convertView;
    }



}
