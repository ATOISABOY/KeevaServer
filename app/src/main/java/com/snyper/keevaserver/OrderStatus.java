package com.snyper.keevaserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.Remote.APIService;
import com.snyper.keevaserver.ViewHolder.OrderViewHolder;
import com.snyper.keevaserver.model.DataMessage;
import com.snyper.keevaserver.model.MyResponse;
import com.snyper.keevaserver.model.Request;
import com.snyper.keevaserver.model.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner,shipperSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        db= FirebaseDatabase.getInstance();
        requests=db.getReference("Requests");



        //init
        mService= Common.getFCMClient();



        ///Init
        recyclerView=(RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseRecyclerOptions< Request> options= new FirebaseRecyclerOptions.Builder< Request>()
                .setQuery( requests, Request.class)
                .build();
        adapter= new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAdress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                //new event buttons
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(),
                                adapter.getItem(position));
                    }
                });
                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent orderDetail = new Intent(OrderStatus.this,OrderDetail.class);
                        Common.currentRequest=model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent trackingOrder = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest=model;
                        startActivity(trackingOrder);
                    }
                });
            }

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private void deleteOrder(String key) {

        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog= new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater= this.getLayoutInflater();
        final View view= inflater.inflate(R.layout.update_order_layout,null);

        spinner=(MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","ON my way","Shipping");

        shipperSpinner=(MaterialSpinner)view.findViewById(R.id.shipperSpinner);

        //load all shippers phone number
        final List<String>shipperList=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot shipperSnapshot:dataSnapshot.getChildren())
                            shipperList.add(shipperSnapshot.getKey());
                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        alertDialog.setView(view);

        final String localKey=key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                if (item.getStatus().equals("2"))
                {
                    //copy item to table "orderneedship"
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                            .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                            .child(localKey)
                            .setValue(item);
                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();
                    //send order status
                    sendOrderStatusToUser(localKey, item);
                    sendOrderShipRequestToShiper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(), item);
                }else {
                    requests.child(localKey).setValue(item);
                    adapter.notifyDataSetChanged();
                    //send order status

                    sendOrderStatusToUser(localKey, item);
                }


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    private void sendOrderShipRequestToShiper(String shipperPhone, Request item) {
        DatabaseReference tokens= db.getReference("Tokens");

        tokens.child(shipperPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                       if (dataSnapshot.exists()){
                           Token token=dataSnapshot.getValue(Token.class);
                           Map<String,String> dataSend= new HashMap<>();
                           dataSend.put("title","Keeva");
                           dataSend.put("message","You have new order to deliver");
                           DataMessage dataMessage=new DataMessage(token.getToken(),dataSend);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                           if (response.body().success==1){
                                               Toast.makeText(OrderStatus.this,"Sent to shipper",Toast.LENGTH_SHORT).show();
                                           }else {

                                               Toast.makeText(OrderStatus.this,"failed to send ",Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR",t.getMessage());
                                       }
                                   });
                       }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens= db.getReference("Tokens");
        tokens.child(item.getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if (dataSnapshot.exists()){
                           Token token=dataSnapshot.getValue(Token.class);
                           Map<String,String> dataSend= new HashMap<>();
                           dataSend.put("title","Keeva");
                           dataSend.put("message","Your Order"+key+"was updated");
                           DataMessage dataMessage=new DataMessage(token.getToken(),dataSend);

                           mService.sendNotification(dataMessage)
                                   .enqueue(new Callback<MyResponse>() {
                                       @Override
                                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                           if (response.body().success==1){
                                               Toast.makeText(OrderStatus.this,"Order was updated",Toast.LENGTH_SHORT).show();
                                           }else {

                                               Toast.makeText(OrderStatus.this,"Order was updated but failed to send ",Toast.LENGTH_SHORT).show();
                                           }
                                       }

                                       @Override
                                       public void onFailure(Call<MyResponse> call, Throwable t) {
                                           Log.e("ERROR",t.getMessage());
                                       }
                                   });
                       }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
