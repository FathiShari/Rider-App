package com.example.riderapp.Common;

import com.example.riderapp.RetrofitController.IFCMClient;
import com.example.riderapp.RetrofitController.IFCMService;

public class Common {
    public static final String driver_table_info = "DriverInformation";
    public static final String rider_table_info = "RiderInformation";
    public static final String diver_location_table = "DriverLocation";
    public static final String pickup_request_table = "PickupRequestRider";
    public static final String driver_vehicles_table = "VehicleInformation";
    public static final String token_table = "Tokeninfo";

    public static final String fcmURl= "https://fcm.googleapis.com/";

    public static IFCMService getfcmService(){

        return IFCMClient.getClient(fcmURl).create(IFCMService.class);
    }

}
