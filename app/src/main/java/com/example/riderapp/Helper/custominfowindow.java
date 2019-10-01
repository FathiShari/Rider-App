package com.example.riderapp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.riderapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class custominfowindow implements GoogleMap.InfoWindowAdapter {

    View myview;

    public custominfowindow(Context context) {
      myview = LayoutInflater.from(context).inflate(R.layout.custom_rider_info,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtPicup = ((TextView)myview.findViewById(R.id.txtpickupinfo));
        txtPicup.setText(marker.getTitle());

        TextView txtSnippet = ((TextView)myview.findViewById(R.id.txtpickupSnippet));
        txtSnippet.setText(marker.getSnippet());

        return myview;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
