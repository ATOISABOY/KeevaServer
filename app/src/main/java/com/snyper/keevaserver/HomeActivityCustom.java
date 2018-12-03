package com.snyper.keevaserver;

import android.*;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.ViewHolder.ShipperOrderViewHolder;
import com.snyper.keevaserver.model.Request;
import com.snyper.keevaserver.model.Token;

public class HomeActivityCustom extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mLastlocation;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shippersOrders;

    FirebaseRecyclerAdapter<Request,ShipperOrderViewHolder> adapter;

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_custom);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                    {
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, Common.REGUEST_CODE
            );
        } else {
            //check if user has play services on fone
            buildLocationCallback();
            buildLocationRequest();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        //Init Firebase
        database=FirebaseDatabase.getInstance();
        shippersOrders=database.getReference(Common.ORDER_NEED_SHIP_TABLE);
        //Views
        recyclerView=(RecyclerView)findViewById(R.id.recycler_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        updateTokenShipper(FirebaseInstanceId.getInstance().getToken());
        loadAllOrderNeedShipping(Common.currentShipper.getPhone());
    }

    private void loadAllOrderNeedShipping(String phone) {

        DatabaseReference orderInChilOfShipper=shippersOrders.child(phone);
        FirebaseRecyclerOptions<Request> listOrders=new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(orderInChilOfShipper,Request.class)
                .build();

        adapter= new FirebaseRecyclerAdapter<Request, ShipperOrderViewHolder>(listOrders) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperOrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAdress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.btnShipping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Common.createShipperorder(adapter.getRef(position).getKey(),
                                Common.currentShipper.getPhone(),
                                mLastlocation);
                        Common.currentRequest=model;
                        Common.currentKey=adapter.getRef(position).getKey();
                        startActivity(new Intent(HomeActivityCustom.this ,TrackingOrderCustom.class));
                    }
                });
            }

            @NonNull
            @Override
            public ShipperOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_view_layout,parent,false);
                return new ShipperOrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void updateTokenShipper(String token) {
        DatabaseReference tokens=database.getReference("Tokens");
        Token data= new Token(token,false);
        tokens.child(Common.currentShipper.getPhone()).setValue(data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Common.REGUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildLocationCallback();
                    buildLocationRequest();
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                }else
                    {
                        Toast.makeText(this, "Allow permissions", Toast.LENGTH_SHORT).show();

                    }
            }
            break;
            default:
                break;
        }

    }

    private void buildLocationCallback() {
        locationCallback=new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastlocation=locationResult.getLastLocation();
                Toast.makeText(HomeActivityCustom.this,new StringBuilder("")
                        .append("/")
                        .append(mLastlocation.getLatitude())
                        .append("/")
                        .append(mLastlocation.getLongitude())
                        .toString(), Toast.LENGTH_SHORT).show();
            }
        };

    }

    private void buildLocationRequest() {
        locationRequest= new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadAllOrderNeedShipping(Common.currentShipper.getPhone());
    }

    @Override
    protected void onStop() {
        if (adapter!=null)
            adapter.stopListening();
        if (fusedLocationProviderClient!=null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }
}
