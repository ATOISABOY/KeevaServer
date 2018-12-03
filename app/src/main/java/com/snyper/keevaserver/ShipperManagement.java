package com.snyper.keevaserver;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;
import android.telephony.SmsManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.ViewHolder.ShipperViewHolder;
import com.snyper.keevaserver.model.Shipper;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

public class ShipperManagement extends AppCompatActivity {

    FloatingActionButton fabAdd;
    DatabaseReference shippers;
    FirebaseDatabase database;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS=0;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    public boolean isCreatedSuccessfully=false;

    FirebaseRecyclerAdapter<Shipper,ShipperViewHolder>adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_management);

        fabAdd=(FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatShipperLayout();
            }
        });

        recyclerView=(RecyclerView)findViewById(R.id.recycler_shippers);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        database=FirebaseDatabase.getInstance();
        shippers=database.getReference(Common.SHIPPERS_TABLE);

        //load all shippers
        loadAllShippers();
    }

    private void loadAllShippers() {

        FirebaseRecyclerOptions<Shipper> allShipper= new FirebaseRecyclerOptions.Builder<Shipper>()
                .setQuery(shippers,Shipper.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(allShipper) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder holder, final int position, @NonNull final Shipper model) {
                holder.shipper_phone.setText(model.getPhone());
                holder.shipper_name.setText(model.getName());

                holder.btn_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(adapter.getRef(position).getKey(),model);
                    }
                });

                holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeShipper(adapter.getRef(position).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shipper_layout,parent,false);

                return new ShipperViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void showEditDialog(String key,Shipper model) {
        AlertDialog.Builder create_shipper_dialog= new AlertDialog.Builder(ShipperManagement.this);
        create_shipper_dialog.setTitle("Update Shipper");

        LayoutInflater inflater=this.getLayoutInflater();
        View view= inflater.inflate(R.layout.create_shipper_layout,null);

        final MaterialEditText edtName=(MaterialEditText)view.findViewById(R.id.edtName);
        final MaterialEditText edtPhone=(MaterialEditText)view.findViewById(R.id.edtPhone);
        final MaterialEditText edtPassword=(MaterialEditText)view.findViewById(R.id.edtPassword);


        //set data
        edtName.setText(model.getName());
        edtPhone.setText(model.getPhone());
        edtPassword.setText(model.getPassword());

        create_shipper_dialog.setView(view);

        create_shipper_dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);
        create_shipper_dialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                Map<String,Object>update= new HashMap<>();
                update.put("name",edtName.getText().toString());
                update.put("phone",edtPhone.getText().toString());
                update.put("password",edtPassword.getText().toString());


                shippers.child(edtPhone.getText().toString())
                        .updateChildren(update)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(ShipperManagement.this, "Shipper details updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        create_shipper_dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        create_shipper_dialog.show();

    }

    private void removeShipper(String key) {
        shippers.child(key)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ShipperManagement.this, "removed successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.notifyDataSetChanged();

        
    }

    private void showCreatShipperLayout() {
        AlertDialog.Builder create_shipper_dialog = new AlertDialog.Builder(ShipperManagement.this);
        create_shipper_dialog.setTitle("Create Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_shipper_layout, null);
        SwitchButton switchButton = (SwitchButton) view.findViewById(R.id.switch_button);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {

                isCreatedSuccessfully = true;
                // Toast.makeText(ShipperManagement.this, "sms can be hr", Toast.LENGTH_SHORT).show();
            }
        });

        final MaterialEditText edtName = (MaterialEditText) view.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) view.findViewById(R.id.edtPhone);
        final MaterialEditText edtPassword = (MaterialEditText) view.findViewById(R.id.edtPassword);

        create_shipper_dialog.setView(view);

        create_shipper_dialog.setIcon(R.drawable.ic_local_shipping_black_24dp);
        create_shipper_dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (edtName != null && edtPhone != null && edtPassword != null)
                {
                    dialog.dismiss();
                  Shipper shipper = new Shipper();
                  shipper.setName(edtName.getText().toString());
                  shipper.setPassword(edtPassword.getText().toString());
                  shipper.setPhone(edtPhone.getText().toString());

                  shippers.child(edtPhone.getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //sendThetext();
                                if (isCreatedSuccessfully) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(edtPhone.getText().toString(), null,
                                            "Your phone number is" + edtPhone.getText().toString() + "and your password is"
                                                    + edtPassword.getText().toString() + " and you can login to keeva business to do delivery", null, null);
                                }
                                Toast.makeText(ShipperManagement.this, "Shipper created successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                  });

                }else {
                    Toast.makeText(ShipperManagement.this, "Some fields are empty !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        create_shipper_dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        create_shipper_dialog.show();



    }




}
