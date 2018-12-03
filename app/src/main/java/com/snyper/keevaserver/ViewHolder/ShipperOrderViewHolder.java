package com.snyper.keevaserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.snyper.keevaserver.R;

/**
 * Created by stephen snyper on 11/29/2018.
 */

public class ShipperOrderViewHolder extends RecyclerView.ViewHolder

{

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAdress;

    public Button btnShipping;


    public ShipperOrderViewHolder(View itemView) {
        super(itemView);
        txtOrderAdress=(TextView) itemView.findViewById(R.id.order_ship_to);
        txtOrderId=(TextView) itemView.findViewById(R.id.order_name);
        txtOrderStatus=(TextView) itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView) itemView.findViewById(R.id.order_phone);


        btnShipping=(Button)itemView.findViewById(R.id.btnShipping);


    }
}

