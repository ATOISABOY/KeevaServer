package com.snyper.keevaserver.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.snyper.keevaserver.Remote.APIService;
import com.snyper.keevaserver.Remote.FCMRetrofitClient;
import com.snyper.keevaserver.Remote.IGeoCoordinates;
import com.snyper.keevaserver.Remote.RetrofitClient;
import com.snyper.keevaserver.model.Request;
import com.snyper.keevaserver.model.Shipper;
import com.snyper.keevaserver.model.ShippingInformation;
import com.snyper.keevaserver.model.User;

/**
 * Created by stephen snyper on 9/11/2018.
 */

public class Common {

    public static final String SHIPPERS_TABLE = "Shippers";
    public static final String ORDER_NEED_SHIP_TABLE= "OrdersNeedShip";
    public static final String SHIPPER_INFO_TABLE= "ShippingOrders";

    public static final int REGUEST_CODE =1000 ;
    public static Shipper currentShipper;

    public static User currentUser;
    public static Request currentRequest;
    public static String currentKey;

    public static String PHONE_TEXT="userPhone";
    public static String topicName="News";

    public static final String UPDATE="Update";
    public static final String DELETE="Delete";

    public static final int PICK_IMAGE_REQUEST=71;

    public static final String baseUrl="https://maps.googleapis.com";
    public static final String fcmUrl="https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code){
        if (code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On my way";
        else
            return "Shipping";
    }

    public static IGeoCoordinates getGeoCodeService(){

        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMClient(){

        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth, int newHeight){

        Bitmap scaledBitmap= Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX= newWidth/(float)bitmap.getWidth();
        float scaleY= newHeight/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas= new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0, new Paint(Paint.FILTER_BITMAP_FLAG));


        return scaledBitmap;





    }

    public static void createShipperorder(String key, String phone, Location mLastlocation) {

        ShippingInformation shippingInformation = new ShippingInformation();
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperPhone(phone);
        shippingInformation.setLat(mLastlocation.getLatitude());
        shippingInformation.setLng(mLastlocation.getLongitude());

        //creating item for shipperinformation table
        FirebaseDatabase.getInstance()
                .getReference(SHIPPER_INFO_TABLE)
                .child(key)
                .setValue(shippingInformation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });

    }
}
