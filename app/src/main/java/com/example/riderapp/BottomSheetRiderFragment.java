package com.example.riderapp;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {

    String mLocation,mDestination;

public static BottomSheetRiderFragment newInstance(String location,String destination) {


    BottomSheetRiderFragment b = new BottomSheetRiderFragment();
    Bundle args = new Bundle();
    args.putString("location",location );
    args.putString("destination",destination );
    b.setArguments(args);
    return b;
}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       mLocation = getArguments().getString("location");
       mDestination = getArguments().getString("destination");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout,container,false);
        TextView location = (TextView)view.findViewById(R.id.txtLocation);
        TextView destination = (TextView)view.findViewById(R.id.txtDestination);
        TextView Calculation = (TextView)view.findViewById(R.id.txtcCalc);

        //set data

        location.setText(mLocation);
        destination.setText(mDestination);

        return view;
    }
}
