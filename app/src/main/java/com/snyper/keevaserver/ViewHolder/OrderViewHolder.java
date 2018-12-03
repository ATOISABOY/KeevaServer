package com.snyper.keevaserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.snyper.keevaserver.Interface.ItemClickListener;
import com.snyper.keevaserver.R;

/**
 * Created by stephen snyper on 9/17/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder

       {

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAdress;

    public Button btnEdit,btnRemove,btnDetail,btnDirection;


    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderAdress=(TextView) itemView.findViewById(R.id.order_ship_to);
        txtOrderId=(TextView) itemView.findViewById(R.id.order_name);
        txtOrderStatus=(TextView) itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView) itemView.findViewById(R.id.order_phone);


        btnEdit=(Button)itemView.findViewById(R.id.btnEdit);
        btnRemove=(Button)itemView.findViewById(R.id.btnRemove);
        btnDetail=(Button)itemView.findViewById(R.id.btnDetail);
        btnDirection=(Button)itemView.findViewById(R.id.btnDirection);

    }
    }
