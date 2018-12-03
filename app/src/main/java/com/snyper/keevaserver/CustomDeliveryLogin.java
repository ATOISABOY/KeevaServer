package com.snyper.keevaserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keevaserver.Common.Common;
import com.snyper.keevaserver.model.Shipper;

import info.hoang8f.widget.FButton;

public class CustomDeliveryLogin extends AppCompatActivity {


    FButton btn_signIn;
    MaterialEditText edt_phone,edt_password;

    FirebaseDatabase database;
    DatabaseReference shippers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_delivery_login);

        btn_signIn=(FButton)findViewById(R.id.btnSignInC);
        edt_password=(MaterialEditText)findViewById(R.id.edtPasswordC);
        edt_phone=(MaterialEditText)findViewById(R.id.edtPhoneC);

        //firebase initialization
        database=FirebaseDatabase.getInstance();
        shippers=database.getReference(Common.SHIPPERS_TABLE);

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(edt_phone.getText().toString(),edt_password.getText().toString());
            }
        });
    }

    private void login(String phone, final String password) {
        shippers.child(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            Shipper shipper=dataSnapshot.getValue(Shipper.class);
                            if (shipper.getPassword().equals(password)){

                                startActivity(new Intent(CustomDeliveryLogin.this,HomeActivityCustom.class));
                                Common.currentShipper=shipper;
                                finish();
                            }

                        }else {

                            Toast.makeText(CustomDeliveryLogin.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
