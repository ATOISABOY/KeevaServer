package com.snyper.keevaserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.snyper.keevaserver.Interface.ItemClickListener;
import com.snyper.keevaserver.R;
import com.suke.widget.SwitchButton;

import info.hoang8f.widget.FButton;

/**
 * Created by stephen snyper on 11/18/2018.
 */

public class ShipperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

public TextView shipper_name,shipper_phone;
public FButton btn_edit,btn_remove;
public SwitchButton switchButton;
private ItemClickListener itemClickListener;

    public ShipperViewHolder(View itemView) {
        super(itemView);

        switchButton=(SwitchButton)itemView.findViewById(R.id.switch_button);
        shipper_name=(TextView)itemView.findViewById(R.id.shipper_name);
        shipper_phone=(TextView)itemView.findViewById(R.id.shipper_phone);
        btn_edit=(FButton)itemView.findViewById(R.id.btnEdit);
        btn_remove=(FButton)itemView.findViewById(R.id.btnRemove);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
